package com.example.oiso.common.safety;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SafetyKoreaService {

    private static final String RESULT_CODE_SUCCESS = "2000"; // Safety Korea API 성공 코드
    private static final String RESULT_CODE_NO_DATA = "2004"; // Safety Korea API 조회 결과 없음 코드

    private static final String CONDITION_KEY_ALL = "all"; // API 검색 조건: 전체 검색
    private static final String CONDITION_KEY_MODEL_NAME = "recallModelName"; // API 검색 조건: 모델명 검색

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2); // API 서버 연결 시 최대 대기 시간 2초
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(4); // API 응답 읽기 최대 대기 시간 4초
    private static final Duration CACHE_TTL = Duration.ofMinutes(30); // 같은 상품명 검사 결과를 30분 동안 캐시

    private final RestClient restClient; // Safety Korea API를 실제 호출하는 객체
    private final ObjectMapper objectMapper; // API 응답 JSON 문자열을 DTO 객체로 변환하는 객체
    private final String authKey; // Safety Korea API 인증키
    private final String recallListUrl; // Safety Korea 국내리콜정보 API URL
    private final ConcurrentHashMap<String, CachedSafetyKoreaResult> cache = new ConcurrentHashMap<>(); // 상품명별 검사 결과 캐시 저장소

    public SafetyKoreaService(@Value("${safety-korea.auth-key}") String authKey, // application.yml의 safety-korea.auth-key 값 주입
                              @Value("${safety-korea.recall-list-url}") String recallListUrl) { // application.yml의 recall-list-url 값 주입
        this.restClient = RestClient.builder() // RestClient 생성 시작
                .requestFactory(createRequestFactory()) // 기존과 동일하게 타임아웃 설정이 적용된 요청 Factory 사용
                .build(); // Safety Korea API 호출용 RestClient 생성
        this.objectMapper = new ObjectMapper(); // JSON 파싱용 ObjectMapper 생성
        this.authKey = authKey; // 인증키 필드에 저장
        this.recallListUrl = recallListUrl; // API URL 필드에 저장
    }

    public SafetyKoreaCheckResult checkRecallProduct(String productName) { // 상품명이 리콜 상품인지 검사하는 메인 메서드
        if (productName == null || productName.trim().isEmpty()) { // 상품명이 없으면 검사할 수 없으므로
            return SafetyKoreaCheckResult.pass(); // 차단하지 않고 통과 처리
        }

        if (authKey == null || authKey.trim().isEmpty()) { // Safety Korea 인증키가 없으면
            System.out.println("[SafetyKorea] 인증키가 비어있어 검사를 통과 처리합니다."); // 콘솔에 안내 로그 출력
            return SafetyKoreaCheckResult.apiErrorPass(); // API 문제로 보고 차단하지 않고 통과 처리
        }

        String trimmedProductName = productName.trim(); // 상품명 앞뒤 공백 제거
        String cacheKey = normalize(trimmedProductName); // 캐시에 사용할 상품명 key를 정규화해서 생성

        if (cacheKey.length() < 3) { // 정규화한 상품명이 너무 짧으면 검색 정확도가 낮으므로
            return SafetyKoreaCheckResult.pass(); // 검사하지 않고 통과 처리
        }

        CachedSafetyKoreaResult cachedResult = cache.get(cacheKey); // 캐시에 기존 검사 결과가 있는지 확인
        if (cachedResult != null && !cachedResult.isExpired()) { // 캐시가 있고 아직 만료되지 않았다면
            return cachedResult.getResult(); // API를 다시 호출하지 않고 캐시 결과 반환
        }

        try { // API 호출 및 검사 중 예외 처리를 위한 try 시작
            List<String> searchValues = buildSearchValues(trimmedProductName); // 상품명에서 API 검색어 후보 목록 생성
            List<String> conditionKeys = buildConditionKeys(); // API 검색 조건 목록 생성

            SafetyKoreaRecallItemDto matchedItem = findMatchedRecallItem(trimmedProductName, searchValues, conditionKeys); // API 검색 결과 중 실제 매칭되는 리콜 상품 찾기

            if (matchedItem == null) { // 매칭된 리콜 상품이 없으면
                SafetyKoreaCheckResult passResult = SafetyKoreaCheckResult.pass(); // 통과 결과 생성
                cache.put(cacheKey, new CachedSafetyKoreaResult(passResult)); // 통과 결과를 캐시에 저장
                return passResult; // 통과 결과 반환
            }

            System.out.println("[SafetyKorea] 리콜 상품 차단: " // 리콜 상품으로 차단될 경우 콘솔 로그 출력
                    + "상품명=" + nullToEmpty(matchedItem.getRecallProductName()) // API 응답의 리콜 상품명 출력
                    + ", 브랜드=" + nullToEmpty(matchedItem.getRecallBrandName()) // API 응답의 브랜드명 출력
                    + ", 모델명=" + nullToEmpty(matchedItem.getRecallModelName()) // API 응답의 모델명 출력
                    + ", recallUid=" + nullToEmpty(matchedItem.getRecallUid())); // API 응답의 리콜 고유번호 출력

            SafetyKoreaCheckResult blockedResult = SafetyKoreaCheckResult.blocked( // 리콜 상품 차단 결과 생성
                    matchedItem.getRecallUid(), // 리콜 고유번호
                    matchedItem.getRecallProductName(), // 리콜 제품명
                    matchedItem.getRecallBrandName(), // 리콜 브랜드명
                    matchedItem.getRecallModelName(), // 리콜 모델명
                    matchedItem.getRecallTypeName(), // 리콜 종류
                    matchedItem.getRecallCmpnyName(), // 리콜 업체명
                    matchedItem.getPublishDate() // 리콜 공표일
            );

            cache.put(cacheKey, new CachedSafetyKoreaResult(blockedResult)); // 차단 결과를 캐시에 저장
            return blockedResult; // 차단 결과 반환

        } catch (RestClientException e) { // RestClient로 API 호출하다가 오류가 난 경우
            System.out.println("[SafetyKorea] API 호출 오류로 검사를 통과 처리합니다. " + e.getMessage()); // API 호출 오류 로그 출력
            return SafetyKoreaCheckResult.apiErrorPass(); // API 오류 때문에 상품 등록 전체가 막히지 않도록 통과 처리
        } catch (Exception e) { // 그 외 검사 중 예상치 못한 오류가 난 경우
            System.out.println("[SafetyKorea] 검사 중 오류로 검사를 통과 처리합니다. " + e.getMessage()); // 일반 오류 로그 출력
            return SafetyKoreaCheckResult.apiErrorPass(); // 오류 발생 시 차단하지 않고 통과 처리
        }
    }

    private SimpleClientHttpRequestFactory createRequestFactory() { // RestClient의 타임아웃 설정 객체 생성
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory(); // 기본 요청 객체 생성
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT); // API 연결 타임아웃 설정
        requestFactory.setReadTimeout(READ_TIMEOUT); // API 응답 읽기 타임아웃 설정
        return requestFactory; // 타임아웃 설정이 적용된 객체 반환
    }

    private List<String> buildConditionKeys() { // Safety Korea API에 사용할 검색 조건 목록 생성
        List<String> conditionKeys = new ArrayList<>(); // 검색 조건 리스트 생성
        conditionKeys.add(CONDITION_KEY_ALL); // 전체 검색 조건 추가
        conditionKeys.add(CONDITION_KEY_MODEL_NAME); // 모델명 검색 조건 추가
        return conditionKeys; // 검색 조건 리스트 반환
    }

    private List<String> buildSearchValues(String productName) { // 상품명에서 API 검색어 후보들을 만드는 메서드
        Set<String> searchValues = new LinkedHashSet<>(); // 중복 없이 순서를 유지하는 검색어 Set 생성

        String trimmed = productName == null ? "" : productName.trim(); // 상품명이 null이면 빈 문자열, 아니면 공백 제거

        if (trimmed.isEmpty()) { // 정리한 상품명이 비어 있으면
            return new ArrayList<>(); // 빈 검색어 리스트 반환
        }

        searchValues.add(trimmed); // 원본 상품명을 첫 번째 검색어로 추가

        String removedParentheses = trimmed // 괄호나 특수 괄호를 공백으로 바꾼 검색어 생성 시작
                .replace("(", " ") // 여는 소괄호 제거
                .replace(")", " ") // 닫는 소괄호 제거
                .replace("[", " ") // 여는 대괄호 제거
                .replace("]", " ") // 닫는 대괄호 제거
                .replace("{", " ") // 여는 중괄호 제거
                .replace("}", " ") // 닫는 중괄호 제거
                .trim(); // 앞뒤 공백 제거

        if (isUsefulSearchValue(removedParentheses)) { // 괄호 제거 검색어가 실제 검색에 쓸 만하면
            searchValues.add(removedParentheses); // 검색어 후보에 추가
        }

        addParenthesesWords(trimmed, searchValues); // 괄호 안에 들어있는 단어를 검색어 후보에 추가
        addLikelyModelPrefix(removedParentheses, searchValues); // 앞쪽 두 단어를 모델명 후보로 추가

        return new ArrayList<>(searchValues); // Set을 List로 바꿔 반환
    }

    private void addParenthesesWords(String value, Set<String> searchValues) { // 괄호 안 단어를 검색어 후보에 추가하는 메서드
        int startIndex = value.indexOf("("); // 여는 괄호 위치 찾기
        int endIndex = value.indexOf(")"); // 닫는 괄호 위치 찾기

        if (startIndex >= 0 && endIndex > startIndex) { // 괄호가 정상적으로 존재하면
            String inside = value.substring(startIndex + 1, endIndex).trim(); // 괄호 안 문자열 추출

            if (isUsefulSearchValue(inside)) { // 괄호 안 문자열이 검색에 쓸 만하면
                searchValues.add(inside); // 검색어 후보에 추가
            }
        }
    }

    private void addLikelyModelPrefix(String value, Set<String> searchValues) { // 상품명 앞쪽 단어를 모델명 후보로 추가하는 메서드
        if (value == null || value.trim().isEmpty()) { // 값이 없으면
            return; // 작업 종료
        }

        String[] words = value.trim().split("\\s+"); // 공백 기준으로 단어 분리

        if (words.length < 2) { // 단어가 2개 미만이면
            return; // 모델명 후보를 만들 수 없으므로 종료
        }

        String firstTwoWords = (words[0] + " " + words[1]).trim(); // 앞쪽 두 단어를 합쳐 모델명 후보 생성

        if (isUsefulSearchValue(firstTwoWords) && containsLetterOrDigit(firstTwoWords)) { // 검색어로 쓸 만하고 문자/숫자가 있으면
            searchValues.add(firstTwoWords); // 검색어 후보에 추가
        }
    }

    private boolean isUsefulSearchValue(String value) { // 검색어로 쓸 만한 값인지 판단
        if (value == null) { // 값이 null이면
            return false; // 쓸 수 없음
        }

        String trimmed = value.trim(); // 앞뒤 공백 제거

        if (trimmed.length() < 2) { // 길이가 너무 짧으면
            return false; // 검색어로 사용하지 않음
        }

        return normalize(trimmed).length() >= 2; // 정규화 후 길이가 2 이상이면 사용 가능
    }

    private boolean containsLetterOrDigit(String value) { // 문자열에 문자나 숫자가 들어있는지 확인
        if (value == null) { // 값이 null이면
            return false; // 문자/숫자가 없다고 처리
        }

        for (char ch : value.toCharArray()) { // 문자열의 문자들을 하나씩 반복
            if (Character.isLetterOrDigit(ch)) { // 문자 또는 숫자가 있으면
                return true; // true 반환
            }
        }

        return false; // 끝까지 문자/숫자가 없으면 false 반환
    }

    private SafetyKoreaRecallItemDto findMatchedRecallItem(String productName, // 사용자가 입력한 상품명
                                                           List<String> searchValues, // API 검색어 후보 목록
                                                           List<String> conditionKeys) { // API 검색 조건 목록
        if (searchValues == null || searchValues.isEmpty()) { // 검색어 후보가 없으면
            return null; // 매칭 상품 없음
        }

        if (conditionKeys == null || conditionKeys.isEmpty()) { // 검색 조건이 없으면
            return null; // 매칭 상품 없음
        }

        String normalizedProductName = normalize(productName); // 사용자가 입력한 상품명을 비교하기 쉽게 정규화

        for (String searchValue : searchValues) { // 검색어 후보들을 하나씩 반복
            for (String conditionKey : conditionKeys) { // 검색 조건들을 하나씩 반복
                List<SafetyKoreaRecallItemDto> recallItems = requestRecallItems(conditionKey, searchValue); // API 호출 후 리콜 상품 목록 조회

                for (SafetyKoreaRecallItemDto item : recallItems) { // 조회된 리콜 상품들을 하나씩 반복
                    if (item == null) { // item이 null이면
                        continue; // 건너뜀
                    }

                    if (isStrongMatched(normalizedProductName, item)) { // 사용자가 입력한 상품명과 리콜 상품이 강하게 매칭되면
                        return item; // 해당 리콜 상품 반환
                    }
                }
            }
        }

        return null; // 모든 검색 결과에서 매칭되는 리콜 상품이 없으면 null 반환
    }

    private List<SafetyKoreaRecallItemDto> requestRecallItems(String conditionKey, String conditionValue) { // API 호출 후 리콜 상품 목록만 추출
        SafetyKoreaRecallResponseDto response = requestRecallList(conditionKey, conditionValue); // Safety Korea API 호출

        if (response == null) { // 응답이 null이면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        if (RESULT_CODE_NO_DATA.equals(response.getResultCode())) { // API 결과 코드가 조회 결과 없음이면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        if (!RESULT_CODE_SUCCESS.equals(response.getResultCode())) { // API 결과 코드가 성공이 아니면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        if (!response.hasRecallData()) { // 응답 안에 리콜 데이터가 없으면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        return response.getResultData(); // 실제 리콜 상품 목록 반환
    }

    private SafetyKoreaRecallResponseDto requestRecallList(String conditionKey, String conditionValue) { // Safety Korea API를 실제 호출하는 메서드
        String requestUrl = UriComponentsBuilder // API 요청 URL 생성 시작
                .fromHttpUrl(recallListUrl) // 기본 API URL 설정
                .queryParam("conditionKey", conditionKey) // 검색 조건 query parameter 추가
                .queryParam("conditionValue", conditionValue) // 검색어 query parameter 추가
                .encode(StandardCharsets.UTF_8) // 한글 검색어가 깨지지 않도록 UTF-8 인코딩
                .toUriString(); // 최종 URL 문자열 생성

        String rawBody = restClient.get() // RestClient로 실제 외부 API GET 요청 준비
                .uri(requestUrl) // 요청 URL 설정
                .header("AuthKey", authKey.trim()) // Safety Korea 인증키를 요청 헤더에 설정
                .retrieve() // API 요청 실행 후 응답 추출 준비
                .body(String.class); // 응답 본문을 String 형태로 받음

        if (rawBody == null || rawBody.isBlank()) { // 응답 본문이 비어 있으면
            return null; // null 반환
        }

        try { // JSON 파싱 예외 처리를 위한 try
            return objectMapper.readValue(rawBody, SafetyKoreaRecallResponseDto.class); // JSON 응답 문자열을 Response DTO로 변환
        } catch (Exception e) { // JSON 파싱 중 오류 발생 시
            System.out.println("[SafetyKorea] 응답 파싱 오류: " + e.getMessage()); // 파싱 오류 로그 출력
            return null; // 파싱 실패 시 null 반환
        }
    }

    private boolean isStrongMatched(String normalizedProductName, SafetyKoreaRecallItemDto item) { // API 결과가 실제 입력 상품과 강하게 일치하는지 판단
        String recallProductName = normalize(item.getRecallProductName()); // API 리콜 제품명 정규화
        String recallBrandName = normalize(item.getRecallBrandName()); // API 브랜드명 정규화
        String recallModelName = normalize(item.getRecallModelName()); // API 모델명 정규화

        if (!recallModelName.isEmpty()) { // 리콜 모델명이 있으면
            if (normalizedProductName.contains(recallModelName)) { // 입력 상품명 안에 리콜 모델명이 포함되면
                return true; // 매칭으로 판단
            }

            if (recallModelName.contains(normalizedProductName)) { // 리콜 모델명 안에 입력 상품명이 포함되면
                return true; // 매칭으로 판단
            }
        }

        if (!recallProductName.isEmpty()) { // 리콜 제품명이 있으면
            if (normalizedProductName.contains(recallProductName)) { // 입력 상품명 안에 리콜 제품명이 포함되면
                return true; // 매칭으로 판단
            }

            if (recallProductName.contains(normalizedProductName) && normalizedProductName.length() >= 4) { // 리콜 제품명 안에 입력 상품명이 포함되고 입력명이 충분히 길면
                return true; // 매칭으로 판단
            }
        }

        return !recallBrandName.isEmpty() // 브랜드명이 비어있지 않고
                && !recallProductName.isEmpty() // 제품명도 비어있지 않고
                && normalizedProductName.contains(recallBrandName) // 입력 상품명에 브랜드명이 포함되고
                && normalizedProductName.contains(recallProductName); // 입력 상품명에 제품명도 포함되면 매칭으로 판단
    }

    private String normalize(String value) { // 비교 정확도를 높이기 위해 문자열을 정규화하는 메서드
        if (value == null) { // 값이 null이면
            return ""; // 빈 문자열 반환
        }

        return value // 문자열 정규화 시작
                .trim() // 앞뒤 공백 제거
                .toLowerCase() // 대소문자 비교 차이를 없애기 위해 소문자로 변경
                .replace(" ", "") // 공백 제거
                .replace("-", "") // 하이픈 제거
                .replace("_", "") // 언더바 제거
                .replace("/", "") // 슬래시 제거
                .replace("\\", "") // 역슬래시 제거
                .replace(".", "") // 점 제거
                .replace(",", "") // 쉼표 제거
                .replace("(", "") // 여는 소괄호 제거
                .replace(")", "") // 닫는 소괄호 제거
                .replace("[", "") // 여는 대괄호 제거
                .replace("]", "") // 닫는 대괄호 제거
                .replace("{", "") // 여는 중괄호 제거
                .replace("}", ""); // 닫는 중괄호 제거
    }

    private String nullToEmpty(String value) { // null 문자열을 빈 문자열로 바꿔주는 메서드
        return value == null ? "" : value; // value가 null이면 "", 아니면 원래 value 반환
    }

    private static class CachedSafetyKoreaResult { // Safety Korea 검사 결과를 캐시에 저장하기 위한 내부 클래스

        private final SafetyKoreaCheckResult result; // 캐시에 저장할 검사 결과
        private final LocalDateTime cachedAt; // 캐시에 저장된 시간

        private CachedSafetyKoreaResult(SafetyKoreaCheckResult result) { // 캐시 객체 생성자
            this.result = result; // 검사 결과 저장
            this.cachedAt = LocalDateTime.now(); // 현재 시간을 캐시 저장 시간으로 저장
        }

        private SafetyKoreaCheckResult getResult() { // 캐시에 저장된 검사 결과 반환
            return result; // 검사 결과 반환
        }

        private boolean isExpired() { // 캐시가 만료되었는지 확인
            return cachedAt.plus(CACHE_TTL).isBefore(LocalDateTime.now()); // 저장 시간 + 30분이 현재보다 이전이면 만료
        }
    }
}