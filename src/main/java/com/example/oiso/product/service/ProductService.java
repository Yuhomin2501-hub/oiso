package com.example.oiso.product.service;

import com.example.oiso.product.dto.ProductImageResponseDto;
import com.example.oiso.product.dto.ProductResponseDto;
import com.example.oiso.product.entity.Product;
import com.example.oiso.product.entity.ProductImage;
import com.example.oiso.product.repository.ProductImageRepository;
import com.example.oiso.product.repository.ProductRepository;
import com.example.oiso.report.repository.ReportRepository;
import com.example.oiso.user.entity.User;
import com.example.oiso.user.repository.UserRepository;
import com.example.oiso.common.safety.SafetyKoreaCheckResult;
import com.example.oiso.common.safety.SafetyKoreaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {       // 상품 등록,조회,수정,삭제,이미지 저장,리콜검사 등등의 상품기능의 실제동작

    private static final String CATEGORY_CLOTHES = "의류"; // 허용 카테고리: 의류
    private static final String CATEGORY_ELECTRONICS = "전자기기"; // 허용 카테고리: 전자기기
    private static final String CATEGORY_BOOK = "도서"; // 허용 카테고리: 도서
    private static final String CATEGORY_LIVING = "생활용품"; // 허용 카테고리: 생활용품
    private static final String CATEGORY_ETC = "기타"; // 허용 카테고리: 기타
    private static final int MAX_ADDITIONAL_IMAGES = 10; // 추가 상품 사진 최대 등록 가능 개수

    private final ProductRepository productRepository; // 상품 저장, 조회, 삭제 담당
    private final ProductImageRepository productImageRepository; // 추가 이미지 저장, 조회, 삭제 담당
    private final UserRepository userRepository; // 상품 등록자 이름 조회 담당
    private final BlockedProductLogService blockedProductLogService; // 금지/리콜 상품 차단 로그 저장 담당
    private final ReportRepository reportRepository; // 상품 신고 여부 확인 및 신고 삭제 담당
    private final SafetyKoreaService safetyKoreaService; // Safety Korea API를 통한 리콜 상품 검사 담당

    private final S3Client s3Client; // AWS S3 업로드/삭제에 사용하는 클라이언트
    private final String s3BucketName; // S3 버킷 이름
    private final String awsRegion; // AWS 리전 값

    public ProductService(ProductRepository productRepository, // 상품 Repository 주입
                          ProductImageRepository productImageRepository, // 추가 이미지 Repository 주입
                          UserRepository userRepository, // 사용자 Repository 주입
                          BlockedProductLogService blockedProductLogService, // 차단 로그 Service 주입
                          ReportRepository reportRepository, // 신고 Repository 주입
                          SafetyKoreaService safetyKoreaService, // Safety Korea Service 주입
                          @Value("${cloud.aws.region}") String awsRegion, // application.yml의 AWS 리전 값 주입
                          @Value("${cloud.aws.s3.bucket}") String s3BucketName, // application.yml의 S3 버킷명 주입
                          @Value("${cloud.aws.credentials.access-key}") String accessKey, // application.yml의 AWS Access Key 주입
                          @Value("${cloud.aws.credentials.secret-key}") String secretKey) { // application.yml의 AWS Secret Key 주입
        this.productRepository = productRepository; // ProductRepository 필드에 저장
        this.productImageRepository = productImageRepository; // ProductImageRepository 필드에 저장
        this.userRepository = userRepository; // UserRepository 필드에 저장
        this.blockedProductLogService = blockedProductLogService; // BlockedProductLogService 필드에 저장
        this.reportRepository = reportRepository; // ReportRepository 필드에 저장
        this.safetyKoreaService = safetyKoreaService; // SafetyKoreaService 필드에 저장
        this.awsRegion = awsRegion; // AWS 리전 필드에 저장
        this.s3BucketName = s3BucketName; // S3 버킷명 필드에 저장

        this.s3Client = S3Client.builder() // S3Client 생성 시작
                .region(Region.of(awsRegion)) // 사용할 AWS 리전 설정
                .credentialsProvider( // AWS 인증정보 설정
                        StaticCredentialsProvider.create( // 고정 인증정보 Provider 생성
                                AwsBasicCredentials.create(accessKey, secretKey) // Access Key와 Secret Key로 인증정보 생성
                        )
                )
                .build(); // S3Client 생성 완료
    }

    // 상품 등록
    public String registerProduct(String userEmail, // 상품 등록자 이메일
                                  String productName, // 상품명
                                  String category, // 카테고리
                                  String productDesc, // 상품 설명
                                  MultipartFile mainProductImage, // 대표 상품 이미지
                                  List<MultipartFile> productImages) { // 추가 상품 이미지 목록

        if (mainProductImage == null || mainProductImage.isEmpty()) { // 대표 이미지가 없으면 등록 불가
            return "대표 상품 사진은 필수입니다."; // 대표 이미지 필수 메시지 반환
        }

        if (productName == null || productName.trim().isEmpty()) { // 상품명이 비어 있는지 검사
            return "상품명은 필수입니다."; // 상품명 필수 메시지 반환
        }

        if (category == null || category.trim().isEmpty()) { // 카테고리가 비어 있는지 검사
            return "카테고리는 필수입니다."; // 카테고리 필수 메시지 반환
        }

        String trimmedCategory = category.trim(); // 카테고리 앞뒤 공백 제거

        if (!isValidCategory(trimmedCategory)) { // 허용된 카테고리인지 검사
            return "카테고리는 의류, 전자기기, 도서, 생활용품, 기타 중에서만 선택할 수 있습니다."; // 잘못된 카테고리 메시지 반환
        }

        String blockedKeyword = findBlockedKeyword(productName, trimmedCategory, productDesc); // 금지 키워드 포함 여부 검사
        if (blockedKeyword != null) { // 금지 키워드가 발견된 경우
            blockedProductLogService.saveBlockedProductLog( // 차단된 상품 로그 저장
                    userEmail, // 등록 시도한 사용자 이메일
                    productName, // 상품명
                    trimmedCategory, // 카테고리
                    productDesc, // 상품 설명
                    blockedKeyword, // 발견된 금지 키워드
                    "등록 불가 상품입니다. 금지 품목이 포함되어 있습니다." // 차단 사유
            );
            return "등록 불가 상품입니다. 금지 품목이 포함되어 있습니다."; // 상품 등록 차단 메시지 반환
        }

        SafetyKoreaCheckResult safetyKoreaCheckResult = safetyKoreaService.checkRecallProduct(productName); // Safety Korea API로 리콜 상품 여부 검사
        if (safetyKoreaCheckResult.isBlocked()) { // 리콜 상품으로 판단된 경우
            blockedProductLogService.saveBlockedProductLog( // 리콜 차단 로그 저장
                    userEmail, // 등록 시도한 사용자 이메일
                    productName, // 상품명
                    trimmedCategory, // 카테고리
                    productDesc, // 상품 설명
                    safetyKoreaCheckResult.getBlockedKeyword(), // 리콜 검사에서 매칭된 키워드
                    safetyKoreaCheckResult.getBlockedReason() // 리콜 차단 사유
            );
            return "제품안전정보센터 국내리콜정보에서 리콜 상품으로 확인되어 등록할 수 없습니다."; // 리콜 상품 등록 차단 메시지 반환
        }

        List<MultipartFile> validAdditionalImages = filterValidImages(productImages); // 비어있지 않은 추가 이미지만 필터링

        if (validAdditionalImages.size() > MAX_ADDITIONAL_IMAGES) { // 추가 이미지가 10장을 초과하는지 검사
            return "일반 상품 사진은 최대 10장까지 등록할 수 있습니다."; // 추가 이미지 개수 제한 메시지 반환
        }

        String mainImagePath; // S3에 저장된 대표 이미지 URL을 담을 변수
        try { // 대표 이미지 저장 예외 처리 시작
            mainImagePath = saveProductImage(mainProductImage); // 대표 이미지를 S3에 업로드하고 URL 반환
        } catch (IOException e) { // 이미지 저장 중 오류 발생 시
            return "대표 상품 사진 저장 중 문제가 발생했습니다."; // 대표 이미지 저장 실패 메시지 반환
        }

        Product product = new Product( // 새 상품 Entity 생성
                userEmail, // 등록자 이메일
                productName.trim(), // 앞뒤 공백 제거한 상품명
                trimmedCategory, // 검증된 카테고리
                productDesc == null ? null : productDesc.trim(), // 설명이 null이면 null, 아니면 공백 제거
                mainImagePath, // 대표 이미지 S3 URL
                "판매중", // 최초 상품 상태는 판매중
                LocalDateTime.now() // 상품 등록일
        );

        productRepository.save(product); // 상품 정보를 MariaDB product 테이블에 저장

        try { // 추가 이미지 저장 예외 처리 시작
            saveAdditionalImages(product.getProductId(), validAdditionalImages, 1); // 추가 이미지를 S3와 product_image 테이블에 저장
        } catch (IOException e) { // 추가 이미지 저장 중 오류 발생 시
            deleteImageFile(mainImagePath); // 이미 업로드된 대표 이미지를 S3에서 삭제
            productRepository.deleteById(product.getProductId()); // 이미 저장된 상품 DB 데이터 삭제
            return "일반 상품 사진 저장 중 문제가 발생했습니다."; // 추가 이미지 저장 실패 메시지 반환
        }

        return "상품 등록 완료"; // 상품 등록 성공 메시지 반환
    }

    // 상품 목록 조회
    public String getProductList() { // 문자열 형태의 전체 상품 목록 조회 기능
        List<Product> productList = productRepository.findAllByOrderByProductIdDesc(); // 상품 전체 목록을 최신순으로 조회

        StringBuilder result = new StringBuilder(); // 문자열 결과를 만들기 위한 객체
        int count = 0; // 삭제되지 않은 상품 개수 카운트

        for (Product product : productList) { // 상품 목록 반복
            if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
                continue; // 목록에서 제외
            }

            result.append("상품번호: ").append(product.getProductId()).append("\n"); // 상품번호 추가
            result.append("등록자이름: ").append(resolveUserName(product.getUserEmail())).append("\n"); // 등록자 이름 추가
            result.append("상품명: ").append(product.getProductName()).append("\n"); // 상품명 추가
            result.append("카테고리: ").append(product.getCategory()).append("\n"); // 카테고리 추가
            result.append("설명: ").append(product.getProductDesc()).append("\n"); // 설명 추가
            result.append("대표이미지경로: ").append(product.getProductImagePath()).append("\n"); // 대표 이미지 URL 추가
            result.append("상태: ").append(product.getProductStatus()).append("\n"); // 상품 상태 추가
            result.append("등록일: ").append(product.getRegDt()).append("\n"); // 등록일 추가
            result.append("--------------------").append("\n"); // 상품 구분선 추가
            count++; // 출력 대상 상품 개수 증가
        }

        if (count == 0) { // 출력할 상품이 하나도 없으면
            return "등록된 상품이 없습니다."; // 상품 없음 메시지 반환
        }

        return result.toString(); // 완성된 문자열 반환
    }

    public List<ProductResponseDto> getProductListData() { // 전체 상품 목록 데이터를 DTO 리스트로 조회
        return getProductListData(null); // 검색어 없이 전체 상품 목록 조회
    }

    // 상품 검색
    public List<ProductResponseDto> getProductListData(String keyword) { // 검색어 포함 상품 목록 조회
        List<Product> productList; // 조회된 상품 목록을 담을 변수

        if (keyword == null || keyword.trim().isEmpty()) { // 검색어가 없으면
            productList = productRepository.findAllByOrderByProductIdDesc(); // 전체 상품 최신순 조회
        } else { // 검색어가 있으면
            productList = productRepository.findByProductNameContainingOrderByProductIdDesc(keyword.trim()); // 상품명 포함 검색
        }

        return convertToProductResponseDtoList(productList, false); // 상품 Entity 목록을 프론트용 DTO 목록으로 변환
    }

    // 카테고리별 상품조회
    public List<ProductResponseDto> getCategoryProductListData(String category) { // 카테고리별 상품 목록 조회
        if (category == null || category.trim().isEmpty()) { // 카테고리 값이 없으면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        String trimmedCategory = category.trim(); // 카테고리 앞뒤 공백 제거

        if (!isValidCategory(trimmedCategory)) { // 허용된 카테고리가 아니면
            return new ArrayList<>(); // 빈 리스트 반환
        }

        List<Product> productList = productRepository.findByCategoryOrderByProductIdDesc(trimmedCategory); // 해당 카테고리 상품 최신순 조회
        return convertToProductResponseDtoList(productList, false); // DTO 리스트로 변환해서 반환
    }

    // 메인페이지의 최신 등록 상품 조회
    public List<ProductResponseDto> getLatestProductListData() { // 메인 최신등록상품 목록 조회
        List<Product> productList = productRepository.findLatestNotReportedProducts( // 신고되지 않은 최신 상품 조회
                List.of("판매중", "예약중"), // 판매중/예약중 상태만 조회
                PageRequest.of(0, 10) // 최대 10개만 조회
        );

        return convertToProductResponseDtoList(productList, false); // DTO 리스트로 변환해서 반환
    }


    public String getProductDetail(Integer productId) { // 문자열 형태의 상품 상세 조회
        Optional<Product> optionalProduct = productRepository.findById(productId); // 상품번호로 상품 조회

        if (optionalProduct.isEmpty()) { // 상품이 없으면
            return "해당 상품이 없습니다."; // 상품 없음 메시지 반환
        }

        Product product = optionalProduct.get(); // Optional에서 Product 꺼내기

        if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
            return "해당 상품이 없습니다."; // 없는 상품처럼 처리
        }

        StringBuilder result = new StringBuilder(); // 상품 상세 문자열 생성 객체
        result.append("상품번호: ").append(product.getProductId()).append("\n"); // 상품번호 추가
        result.append("등록자이름: ").append(resolveUserName(product.getUserEmail())).append("\n"); // 등록자 이름 추가
        result.append("상품명: ").append(product.getProductName()).append("\n"); // 상품명 추가
        result.append("카테고리: ").append(product.getCategory()).append("\n"); // 카테고리 추가
        result.append("설명: ").append(product.getProductDesc()).append("\n"); // 설명 추가
        result.append("대표이미지경로: ").append(product.getProductImagePath()).append("\n"); // 대표 이미지 URL 추가
        result.append("상태: ").append(product.getProductStatus()).append("\n"); // 상품 상태 추가
        result.append("등록일: ").append(product.getRegDt()); // 등록일 추가

        return result.toString(); // 상품 상세 문자열 반환
    }

    // 상품 상세 조회
    public ProductResponseDto getProductDetailData(Integer productId, String loginUserEmail) { // 상품 상세 데이터를 DTO로 조회
        Optional<Product> optionalProduct = productRepository.findById(productId); // 상품번호로 상품 조회

        if (optionalProduct.isEmpty()) { // 상품이 없으면
            return null; // null 반환
        }

        Product product = optionalProduct.get(); // Optional에서 Product 꺼내기

        if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
            return null; // null 반환
        }

        boolean myProduct = loginUserEmail != null && loginUserEmail.equals(product.getUserEmail()); // 현재 로그인 사용자의 상품인지 판단
        boolean reported = isReportedProduct(product.getProductId()); // 해당 상품이 신고된 상품인지 확인

        return new ProductResponseDto( // 프론트로 내려줄 상품 상세 DTO 생성
                product.getProductId(), // 상품번호
                product.getUserEmail(), // 등록자 이메일
                resolveUserName(product.getUserEmail()), // 등록자 이름
                product.getProductName(), // 상품명
                product.getCategory(), // 카테고리
                product.getProductDesc(), // 상품 설명
                product.getProductImagePath(), // 대표 이미지 URL
                buildProductImagePathList(product), // 대표 이미지 + 추가 이미지 URL 전체 목록
                buildAdditionalImageResponseList(product.getProductId()), // 추가 이미지 정보 목록
                product.getProductStatus(), // 상품 상태
                product.getRegDt(), // 등록일
                myProduct, // 내 상품 여부
                reported // 신고 여부
        );
    }

    // 내 상품 목록 조회
    public String getMyProductList(String userEmail) { // 문자열 형태의 내 상품 목록 조회
        List<Product> productList = productRepository.findByUserEmailOrderByProductIdDesc(userEmail); // 내 상품 최신순 조회

        StringBuilder result = new StringBuilder(); // 결과 문자열 생성 객체
        int count = 0; // 삭제되지 않은 내 상품 개수

        for (Product product : productList) { // 내 상품 목록 반복
            if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
                continue; // 목록에서 제외
            }

            result.append("상품번호: ").append(product.getProductId()).append("\n"); // 상품번호 추가
            result.append("등록자이름: ").append(resolveUserName(product.getUserEmail())).append("\n"); // 등록자 이름 추가
            result.append("상품명: ").append(product.getProductName()).append("\n"); // 상품명 추가
            result.append("카테고리: ").append(product.getCategory()).append("\n"); // 카테고리 추가
            result.append("설명: ").append(product.getProductDesc()).append("\n"); // 설명 추가
            result.append("대표이미지경로: ").append(product.getProductImagePath()).append("\n"); // 대표 이미지 URL 추가
            result.append("상태: ").append(product.getProductStatus()).append("\n"); // 상품 상태 추가
            result.append("등록일: ").append(product.getRegDt()).append("\n"); // 등록일 추가
            result.append("--------------------").append("\n"); // 상품 구분선 추가
            count++; // 출력 대상 상품 개수 증가
        }

        if (count == 0) { // 내 상품이 하나도 없으면
            return "내가 등록한 상품이 없습니다."; // 내 상품 없음 메시지 반환
        }

        return result.toString(); // 내 상품 목록 문자열 반환
    }

    // 내 상품 수정
    public List<ProductResponseDto> getMyProductListData(String userEmail) { // 프론트용 내 상품 목록 조회
        List<Product> productList = productRepository.findByUserEmailOrderByProductIdDesc(userEmail); // 내 상품 최신순 조회
        return convertToProductResponseDtoList(productList, true); // 내 상품 여부 true로 DTO 변환
    }

    public String updateProduct(Integer productId, // 수정할 상품번호
                                String loginUserEmail, // 현재 로그인한 사용자 이메일
                                String productName, // 수정할 상품명
                                String category, // 수정할 카테고리
                                String productDesc, // 수정할 상품 설명
                                String productStatus, // 수정할 상품 상태
                                MultipartFile mainProductImage, // 새 대표 이미지
                                List<MultipartFile> productImages, // 새 추가 이미지 목록
                                List<Integer> deleteProductImageIds) { // 삭제할 기존 추가 이미지 ID 목록
        Optional<Product> optionalProduct = productRepository.findById(productId); // 수정할 상품 조회

        if (optionalProduct.isEmpty()) { // 상품이 존재하지 않으면
            return "해당 상품이 없습니다."; // 상품 없음 메시지 반환
        }

        Product product = optionalProduct.get(); // Optional에서 Product 꺼내기

        if ("삭제됨".equals(product.getProductStatus())) { // 이미 삭제된 상품이면
            return "삭제된 상품은 수정할 수 없습니다."; // 수정 불가 메시지 반환
        }

        if (!product.getUserEmail().equals(loginUserEmail)) { // 상품 등록자와 로그인 사용자가 다르면
            return "본인이 등록한 상품만 수정할 수 있습니다."; // 권한 없음 메시지 반환
        }

        if (productName == null || productName.trim().isEmpty()) { // 상품명이 비어 있으면
            return "상품명은 필수입니다."; // 상품명 필수 메시지 반환
        }

        if (category == null || category.trim().isEmpty()) { // 카테고리가 비어 있으면
            return "카테고리는 필수입니다."; // 카테고리 필수 메시지 반환
        }

        if (productStatus == null || productStatus.trim().isEmpty()) { // 상품 상태가 비어 있으면
            return "상품 상태는 필수입니다."; // 상품 상태 필수 메시지 반환
        }

        String trimmedCategory = category.trim(); // 카테고리 앞뒤 공백 제거
        String trimmedStatus = productStatus.trim(); // 상품 상태 앞뒤 공백 제거

        if (!isValidCategory(trimmedCategory)) { // 허용된 카테고리인지 검사
            return "카테고리는 의류, 전자기기, 도서, 생활용품, 기타 중에서만 선택할 수 있습니다."; // 잘못된 카테고리 메시지 반환
        }

        if (!isValidProductStatus(trimmedStatus)) { // 허용된 상품 상태인지 검사
            return "변경 가능한 상태는 판매중, 예약중, 거래완료만 가능합니다."; // 잘못된 상태 메시지 반환
        }

        String blockedKeyword = findBlockedKeyword(productName, trimmedCategory, productDesc); // 금지 키워드 검사
        if (blockedKeyword != null) { // 금지 키워드가 포함된 경우
            blockedProductLogService.saveBlockedProductLog( // 차단 로그 저장
                    loginUserEmail, // 수정 시도한 사용자 이메일
                    productName, // 상품명
                    trimmedCategory, // 카테고리
                    productDesc, // 상품 설명
                    blockedKeyword, // 발견된 금지 키워드
                    "등록 불가 상품입니다. 금지 품목이 포함되어 있습니다." // 차단 사유
            );
            return "등록 불가 상품입니다. 금지 품목이 포함되어 있습니다."; // 수정 차단 메시지 반환
        }

        SafetyKoreaCheckResult safetyKoreaCheckResult = safetyKoreaService.checkRecallProduct(productName); // Safety Korea 리콜 상품 검사
        if (safetyKoreaCheckResult.isBlocked()) { // 리콜 상품으로 판단된 경우
            blockedProductLogService.saveBlockedProductLog( // 리콜 상품 차단 로그 저장
                    loginUserEmail, // 수정 시도한 사용자 이메일
                    productName, // 상품명
                    trimmedCategory, // 카테고리
                    productDesc, // 상품 설명
                    safetyKoreaCheckResult.getBlockedKeyword(), // 리콜 매칭 키워드
                    safetyKoreaCheckResult.getBlockedReason() // 리콜 차단 사유
            );
            return "제품안전정보센터 국내리콜정보에서 리콜 상품으로 확인되어 수정할 수 없습니다."; // 리콜 상품 수정 차단 메시지 반환
        }

        List<ProductImage> currentAdditionalImages = // 현재 상품의 기존 추가 이미지 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(productId); // 기존 추가 이미지들을 정렬순으로 조회

        int deleteCount = countOwnedDeleteTargets(productId, deleteProductImageIds); // 실제로 이 상품에 속한 삭제 대상 이미지 개수 계산
        int remainCount = currentAdditionalImages.size() - deleteCount; // 삭제 후 남을 기존 추가 이미지 개수 계산

        List<MultipartFile> validNewAdditionalImages = filterValidImages(productImages); // 새로 추가할 이미지 중 유효한 파일만 필터링

        if (remainCount + validNewAdditionalImages.size() > MAX_ADDITIONAL_IMAGES) { // 남은 이미지 + 새 이미지가 10장을 넘는지 검사
            return "일반 상품 사진은 최대 10장까지 등록할 수 있습니다."; // 추가 이미지 개수 제한 메시지 반환
        }

        String oldMainImagePath = product.getProductImagePath(); // 기존 대표 이미지 URL 저장
        String finalMainImagePath = oldMainImagePath; // 최종 대표 이미지 URL 기본값은 기존 이미지

        if (mainProductImage != null && !mainProductImage.isEmpty()) { // 새 대표 이미지가 들어온 경우
            try { // 새 대표 이미지 저장 예외 처리 시작
                finalMainImagePath = saveProductImage(mainProductImage); // 새 대표 이미지를 S3에 업로드
            } catch (IOException e) { // 업로드 중 오류 발생 시
                return "대표 상품 사진 저장 중 문제가 발생했습니다."; // 대표 이미지 저장 실패 메시지 반환
            }
        }

        deleteSelectedAdditionalImages(productId, deleteProductImageIds); // 사용자가 선택한 기존 추가 이미지 삭제

        int nextImageOrder = getNextImageOrder(productId); // 새 추가 이미지가 들어갈 다음 순서 계산

        try { // 새 추가 이미지 저장 예외 처리 시작
            saveAdditionalImages(productId, validNewAdditionalImages, nextImageOrder); // 새 추가 이미지 S3 업로드 및 DB 저장
        } catch (IOException e) { // 새 추가 이미지 저장 실패 시
            if (mainProductImage != null // 새 대표 이미지가 있었고
                    && !mainProductImage.isEmpty() // 새 대표 이미지가 비어있지 않고
                    && finalMainImagePath != null // 최종 대표 이미지 URL이 존재하고
                    && !finalMainImagePath.equals(oldMainImagePath)) { // 기존 대표 이미지와 다른 경우
                deleteImageFile(finalMainImagePath); // 실패 보상 처리로 새 대표 이미지 S3 삭제
            }
            return "일반 상품 사진 저장 중 문제가 발생했습니다."; // 추가 이미지 저장 실패 메시지 반환
        }

        product.updateProduct( // 상품 기본정보 수정
                productName.trim(), // 수정 상품명
                trimmedCategory, // 수정 카테고리
                productDesc == null ? null : productDesc.trim(), // 수정 설명
                finalMainImagePath // 최종 대표 이미지 URL
        );
        product.updateProductStatus(trimmedStatus); // 상품 상태 수정
        productRepository.save(product); // 수정된 상품 정보를 DB에 저장

        if (mainProductImage != null // 새 대표 이미지가 있었고
                && !mainProductImage.isEmpty() // 새 대표 이미지가 비어있지 않고
                && oldMainImagePath != null // 기존 대표 이미지가 존재하고
                && !oldMainImagePath.equals(finalMainImagePath)) { // 기존 대표 이미지와 새 대표 이미지가 다른 경우
            deleteImageFile(oldMainImagePath); // 기존 대표 이미지를 S3에서 삭제
        }

        return "상품 수정 완료"; // 상품 수정 성공 메시지 반환
    }

    // 상품 삭제
    @Transactional // 상품 삭제 과정 중 오류 발생 시 전체 작업 롤백 처리
    public String deleteProduct(Integer productId, String loginUserEmail) { // 상품 삭제 기능
        Optional<Product> optionalProduct = productRepository.findById(productId); // 삭제할 상품 조회

        if (optionalProduct.isEmpty()) { // 상품이 없으면
            return "해당 상품이 없습니다."; // 상품 없음 메시지 반환
        }

        Product product = optionalProduct.get(); // Optional에서 Product 꺼내기

        if (!product.getUserEmail().equals(loginUserEmail)) { // 상품 등록자와 로그인 사용자가 다르면
            return "본인이 등록한 상품만 삭제할 수 있습니다."; // 권한 없음 메시지 반환
        }

        if ("삭제됨".equals(product.getProductStatus())) { // 이미 삭제 상태라면
            return "이미 삭제된 상품입니다."; // 이미 삭제됨 메시지 반환
        }

        reportRepository.deleteByProductId(productId); // 해당 상품에 연결된 신고 내역 삭제

        deleteAdditionalImageFiles(productId); // 추가 이미지 파일과 product_image DB 데이터 삭제

        if (product.getProductImagePath() != null && !product.getProductImagePath().isBlank()) { // 대표 이미지 URL이 존재하면
            deleteImageFile(product.getProductImagePath()); // 대표 이미지를 S3에서 삭제
        }

        product.updateProductStatus("삭제됨"); // 상품 상태를 삭제됨으로 변경
        productRepository.save(product); // 변경된 상품 상태 저장

        return "상품 삭제 완료"; // 상품 삭제 성공 메시지 반환
    }

    // 상품 '상태' 변경
    public String updateProductStatus(Integer productId, String loginUserEmail, String productStatus) { // 상품 상태만 변경하는 기능
        Optional<Product> optionalProduct = productRepository.findById(productId); // 상품번호로 상품 조회

        if (optionalProduct.isEmpty()) { // 상품이 없으면
            return "해당 상품이 없습니다."; // 상품 없음 메시지 반환
        }

        Product product = optionalProduct.get(); // Optional에서 Product 꺼내기

        if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
            return "삭제된 상품은 상태 변경할 수 없습니다."; // 상태 변경 불가 메시지 반환
        }

        if (!product.getUserEmail().equals(loginUserEmail)) { // 본인 상품이 아니면
            return "본인이 등록한 상품만 상태 변경할 수 있습니다."; // 권한 없음 메시지 반환
        }

        if (!productStatus.equals("판매중") // 판매중이 아니고
                && !productStatus.equals("예약중") // 예약중도 아니고
                && !productStatus.equals("거래완료")) { // 거래완료도 아니면
            return "변경 가능한 상태는 판매중, 예약중, 거래완료만 가능합니다."; // 허용되지 않는 상태 메시지 반환
        }

        product.updateProductStatus(productStatus); // 상품 상태 변경
        productRepository.save(product); // 변경된 상품 저장

        return "상품 상태 변경 완료"; // 상태 변경 성공 메시지 반환
    }


    private List<ProductResponseDto> convertToProductResponseDtoList(List<Product> productList, boolean myProduct) { // Product 목록을 ProductResponseDto 목록으로 변환
        List<ProductResponseDto> result = new ArrayList<>(); // 변환 결과를 담을 리스트

        for (Product product : productList) { // 상품 목록 반복
            if ("삭제됨".equals(product.getProductStatus())) { // 삭제된 상품이면
                continue; // DTO 변환 대상에서 제외
            }

            boolean reported = isReportedProduct(product.getProductId()); // 해당 상품의 신고 여부 확인

            result.add(new ProductResponseDto( // 상품 정보를 프론트용 DTO로 변환해서 리스트에 추가
                    product.getProductId(), // 상품번호
                    product.getUserEmail(), // 등록자 이메일
                    resolveUserName(product.getUserEmail()), // 등록자 이름
                    product.getProductName(), // 상품명
                    product.getCategory(), // 카테고리
                    product.getProductDesc(), // 상품 설명
                    product.getProductImagePath(), // 대표 이미지 URL
                    buildProductImagePathList(product), // 대표 이미지 + 추가 이미지 URL 목록
                    buildAdditionalImageResponseList(product.getProductId()), // 추가 이미지 상세 정보 목록
                    product.getProductStatus(), // 상품 상태
                    product.getRegDt(), // 등록일
                    myProduct, // 내 상품 여부
                    reported // 신고 여부
            ));
        }

        return result; // DTO 리스트 반환
    }


    private boolean isReportedProduct(Integer productId) { // 상품 신고 여부 확인
        if (productId == null) { // 상품번호가 null이면
            return false; // 신고 여부 false 처리
        }

        return reportRepository.existsByProductId(productId); // 해당 상품번호로 신고가 존재하는지 확인
    }

    // 상품 상세 페이지에서 이미지 슬라이더에 보여줄 전체 이미지 url 목록을 만들어줌
    private List<String> buildProductImagePathList(Product product) { // 대표 이미지와 추가 이미지 URL 전체 목록 생성
        List<String> imagePathList = new ArrayList<>(); // 이미지 URL 목록 생성

        if (product.getProductImagePath() != null && !product.getProductImagePath().isBlank()) { // 대표 이미지 URL이 있으면
            imagePathList.add(product.getProductImagePath()); // 대표 이미지를 목록에 먼저 추가
        }

        List<ProductImage> additionalImages = // 추가 이미지 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(product.getProductId()); // 상품번호 기준 추가 이미지 정렬 조회

        for (ProductImage productImage : additionalImages) { // 추가 이미지 반복
            if (productImage.getImagePath() != null && !productImage.getImagePath().isBlank()) { // 추가 이미지 URL이 있으면
                imagePathList.add(productImage.getImagePath()); // 이미지 URL 목록에 추가
            }
        }

        return imagePathList; // 전체 이미지 URL 목록 반환
    }

    private List<ProductImageResponseDto> buildAdditionalImageResponseList(Integer productId) { // 추가 이미지 DTO 목록 생성
        List<ProductImageResponseDto> result = new ArrayList<>(); // 결과 리스트 생성

        List<ProductImage> additionalImages = // 추가 이미지 Entity 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(productId); // 상품번호 기준 추가 이미지 정렬 조회

        for (ProductImage productImage : additionalImages) { // 추가 이미지 반복
            result.add(new ProductImageResponseDto( // 추가 이미지 DTO 생성 후 추가
                    productImage.getProductImageId(), // 추가 이미지 ID
                    productImage.getImagePath(), // 추가 이미지 S3 URL
                    productImage.getImageOrder() // 이미지 순서
            ));
        }

        return result; // 추가 이미지 DTO 목록 반환
    }

    private List<MultipartFile> filterValidImages(List<MultipartFile> imageFiles) { // 실제 파일이 있는 이미지만 걸러내는 기능
        List<MultipartFile> validFiles = new ArrayList<>(); // 유효한 이미지 파일 리스트 생성

        if (imageFiles == null) { // 이미지 목록 자체가 null이면
            return validFiles; // 빈 리스트 반환
        }

        for (MultipartFile imageFile : imageFiles) { // 이미지 파일 목록 반복
            if (imageFile != null && !imageFile.isEmpty()) { // 파일 객체가 있고 비어있지 않으면
                validFiles.add(imageFile); // 유효한 파일로 추가
            }
        }

        return validFiles; // 유효한 이미지 파일 목록 반환
    }

    // 추가 이미지
    private void saveAdditionalImages(Integer productId, // 추가 이미지를 연결할 상품번호
                                      List<MultipartFile> additionalImages, // 추가 이미지 파일 목록
                                      int startOrder) throws IOException { // 시작 이미지 순서, 저장 실패 시 IOException 발생
        if (additionalImages == null || additionalImages.isEmpty()) { // 추가 이미지가 없으면
            return; // 아무 작업 없이 종료
        }

        List<ProductImage> productImages = new ArrayList<>(); // DB에 저장할 ProductImage Entity 목록
        int imageOrder = startOrder; // 이미지 순서 시작값 설정

        for (MultipartFile additionalImage : additionalImages) { // 추가 이미지 반복
            String imagePath = saveProductImage(additionalImage); // 추가 이미지를 S3에 업로드하고 URL 반환

            productImages.add(new ProductImage( // 추가 이미지 Entity 생성 후 리스트에 추가
                    productId, // 연결될 상품번호
                    imagePath, // S3 이미지 URL
                    imageOrder, // 이미지 순서
                    LocalDateTime.now() // 이미지 등록일
            ));

            imageOrder++; // 다음 이미지 순서 증가
        }

        productImageRepository.saveAll(productImages); // 추가 이미지 정보를 product_image 테이블에 한 번에 저장
    }

    private int countOwnedDeleteTargets(Integer productId, List<Integer> deleteProductImageIds) { // 삭제 대상 중 현재 상품에 속한 이미지 개수 계산
        if (deleteProductImageIds == null || deleteProductImageIds.isEmpty()) { // 삭제 대상 ID가 없으면
            return 0; // 삭제 개수 0 반환
        }

        List<ProductImage> targetImages = productImageRepository.findAllById(deleteProductImageIds); // 삭제 요청된 이미지 ID들 조회
        int count = 0; // 실제 삭제 대상 개수

        for (ProductImage productImage : targetImages) { // 조회된 이미지 반복
            if (productId.equals(productImage.getProductId())) { // 현재 상품에 속한 이미지인 경우만
                count++; // 삭제 대상 개수 증가
            }
        }

        return count; // 실제 삭제 대상 개수 반환
    }

    private void deleteSelectedAdditionalImages(Integer productId, List<Integer> deleteProductImageIds) { // 선택된 추가 이미지 삭제
        if (deleteProductImageIds == null || deleteProductImageIds.isEmpty()) { // 삭제할 이미지 ID가 없으면
            return; // 아무 작업 없이 종료
        }

        List<ProductImage> targetImages = productImageRepository.findAllById(deleteProductImageIds); // 삭제 요청된 이미지들 조회

        for (ProductImage productImage : targetImages) { // 삭제 대상 이미지 반복
            if (!productId.equals(productImage.getProductId())) { // 현재 상품의 이미지가 아니면
                continue; // 삭제하지 않고 건너뜀
            }

            deleteImageFile(productImage.getImagePath()); // S3에서 이미지 파일 삭제
            productImageRepository.deleteById(productImage.getProductImageId()); // product_image 테이블에서 이미지 정보 삭제
        }

        reorderAdditionalImages(productId); // 삭제 후 이미지 순서 재정렬
    }

    private int getNextImageOrder(Integer productId) { // 새 추가 이미지가 들어갈 다음 순서 계산
        List<ProductImage> productImages = // 기존 추가 이미지 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(productId); // 기존 추가 이미지 정렬 조회

        if (productImages.isEmpty()) { // 기존 추가 이미지가 없으면
            return 1; // 첫 번째 순서 반환
        }

        return productImages.get(productImages.size() - 1).getImageOrder() + 1; // 마지막 이미지 순서 + 1 반환
    }

    private void reorderAdditionalImages(Integer productId) { // 추가 이미지 순서를 1번부터 다시 정렬
        List<ProductImage> productImages = // 현재 상품의 추가 이미지 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(productId); // 추가 이미지 정렬 조회

        int order = 1; // 새 순서 시작값
        for (ProductImage productImage : productImages) { // 추가 이미지 반복
            productImage.updateImageOrder(order); // 이미지 순서 변경
            order++; // 다음 순서 증가
        }

        productImageRepository.saveAll(productImages); // 재정렬된 이미지 순서 저장
    }

    private void deleteAdditionalImageFiles(Integer productId) { // 특정 상품의 모든 추가 이미지 삭제
        List<ProductImage> productImages = // 삭제할 추가 이미지 목록 변수
                productImageRepository.findByProductIdOrderByImageOrderAsc(productId); // 상품번호 기준 추가 이미지 조회

        for (ProductImage productImage : productImages) { // 추가 이미지 반복
            deleteImageFile(productImage.getImagePath()); // S3에서 이미지 파일 삭제
        }

        productImageRepository.deleteByProductId(productId); // product_image 테이블에서 해당 상품의 추가 이미지 정보 삭제
    }

    private boolean isValidCategory(String category) { // 허용된 카테고리인지 검사
        return CATEGORY_CLOTHES.equals(category) // 의류인지 확인
                || CATEGORY_ELECTRONICS.equals(category) // 전자기기인지 확인
                || CATEGORY_BOOK.equals(category) // 도서인지 확인
                || CATEGORY_LIVING.equals(category) // 생활용품인지 확인
                || CATEGORY_ETC.equals(category); // 기타인지 확인
    }

    private boolean isValidProductStatus(String productStatus) { // 허용된 상품 상태인지 검사
        return "판매중".equals(productStatus) // 판매중인지 확인
                || "예약중".equals(productStatus) // 예약중인지 확인
                || "거래완료".equals(productStatus); // 거래완료인지 확인
    }


    // 금지 키워드 검사
    private String findBlockedKeyword(String productName, String category, String productDesc) { // 금지 키워드 포함 여부 검사
        String name = productName == null ? "" : productName.trim(); // 상품명이 null이면 빈 문자열, 아니면 공백 제거
        String cat = category == null ? "" : category.trim(); // 카테고리가 null이면 빈 문자열, 아니면 공백 제거
        String desc = productDesc == null ? "" : productDesc.trim(); // 설명이 null이면 빈 문자열, 아니면 공백 제거

        String target = name + " " + cat + " " + desc; // 상품명, 카테고리, 설명을 하나의 검사 문자열로 합침

        String[] blockedKeywords = { // 등록을 막을 금지 키워드 목록
                "전자담배", "액상담배", "부탄가스",
                "필로폰", "휘발유",
                "권총", "실탄", "탄환",
                "주류", "소주", "맥주", "와인",
                "도검", "폭죽", "폭탄", "농약",
                "마약", "대마", "총", "술", "담배", "액상", "경유"
        };

        for (String keyword : blockedKeywords) { // 금지 키워드 목록 반복
            if (target.contains(keyword)) { // 검사 문자열에 금지 키워드가 포함되어 있으면
                return keyword; // 발견된 금지 키워드 반환
            }
        }

        return null; // 금지 키워드가 없으면 null 반환
    }

    private String resolveUserName(String userEmail) { // 이메일을 사용자 이름으로 변환
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail); // 이메일로 사용자 조회
        return optionalUser.map(User::getUserName).orElse(userEmail); // 사용자가 있으면 이름, 없으면 이메일 반환
    }

    // 이미지 S3 업로드
    private String saveProductImage(MultipartFile imageFile) throws IOException { // 이미지를 S3에 업로드하고 URL을 반환
        if (imageFile == null || imageFile.isEmpty()) { // 파일이 없거나 비어 있으면
            throw new IOException("empty file"); // 빈 파일 예외 발생
        }

        String contentType = imageFile.getContentType(); // 업로드 파일의 Content-Type 확인
        if (contentType == null || !contentType.startsWith("image/")) { // 이미지 파일이 아니면
            throw new IOException("invalid image type"); // 잘못된 이미지 타입 예외 발생
        }

        String originalFilename = imageFile.getOriginalFilename(); // 원본 파일명 가져오기
        String extension = ""; // 파일 확장자 기본값

        if (originalFilename != null && originalFilename.contains(".")) { // 원본 파일명에 확장자가 있으면
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 마지막 점부터 확장자 추출
        }

        String savedFileName = UUID.randomUUID() + extension; // UUID로 중복 없는 저장 파일명 생성
        String s3Key = "products/" + savedFileName; // S3 안에서 저장될 경로/key 생성

        PutObjectRequest putObjectRequest = PutObjectRequest.builder() // S3 업로드 요청 객체 생성 시작
                .bucket(s3BucketName) // 업로드할 S3 버킷 지정
                .key(s3Key) // S3 저장 경로 지정
                .contentType(contentType) // 이미지 Content-Type 지정
                .build(); // 업로드 요청 객체 생성 완료

        s3Client.putObject( // S3에 파일 업로드 실행
                putObjectRequest, // 업로드 요청 정보
                RequestBody.fromBytes(imageFile.getBytes()) // 업로드할 파일 데이터를 byte 배열로 전달
        );

        return "https://" + s3BucketName + ".s3." + awsRegion + ".amazonaws.com/" + s3Key; // 업로드된 이미지의 접근 URL 반환
    }

    // 이미지 S3 삭제
    private void deleteImageFile(String imagePath) { // S3 이미지 URL을 받아 해당 이미지를 삭제
        if (imagePath == null || imagePath.isBlank()) { // 이미지 경로가 없으면
            return; // 삭제할 것이 없으므로 종료
        }

        String marker = ".amazonaws.com/"; // S3 URL에서 key 부분을 찾기 위한 기준 문자열
        int markerIndex = imagePath.indexOf(marker); // 기준 문자열 위치 찾기

        if (markerIndex == -1) { // S3 URL 형식이 아니면
            return; // 삭제하지 않고 종료
        }

        String s3Key = imagePath.substring(markerIndex + marker.length()); // URL에서 S3 key 부분만 추출

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder() // S3 삭제 요청 객체 생성 시작
                .bucket(s3BucketName) // 삭제할 파일이 있는 버킷 지정
                .key(s3Key) // 삭제할 S3 key 지정
                .build(); // 삭제 요청 객체 생성 완료

        s3Client.deleteObject(deleteObjectRequest); // S3에서 해당 이미지 삭제
    }
}