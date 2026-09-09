package com.example.oiso.common.safety;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SafetyKoreaRecallResponseDto {     // safety korea api 전체 응답을 받는 DTO

    private String resultCode;  // API 응답 결과 코드, 2000은 성공 2004는 조회결과없음
    private String resultMsg;   // API 응답 결과 메시지
    private List<SafetyKoreaRecallItemDto> resultData = new ArrayList<>();  // 리콜 상품 목록 데이터

    public SafetyKoreaRecallResponseDto() {     // ObjectMapper가 JSON 응답을 DTO로 변환할 때 사용하는 기본 생성자
    }

    public String getResultCode() {
        return resultCode;
    }   // API 응답 결과 코드 반환

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }   // JSON에서 받은 결과 코드를 저장

    public String getResultMsg() {
        return resultMsg;
    }   // API 응답 결과 메시지 반환

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }   // JSON에서 받은 결과 메시지를 저장

    public List<SafetyKoreaRecallItemDto> getResultData() {
        return resultData;
    }    // 리콜 상품 목록 반환

    // resultData가 null이면 빈 리스트로 저장해서 NullPointerException 방지
    public void setResultData(List<SafetyKoreaRecallItemDto> resultData) {
        this.resultData = resultData == null ? new ArrayList<>() : resultData;
    }

    // api 호출 성공 여부 확인
    public boolean isSuccess() {
        return "2000".equals(resultCode);
    }

    // 조회결과 없음
    public boolean isNoData() {
        return "2004".equals(resultCode);
    }

    // 실제 리콜 데이터 유무 확인, NULL이 아니고 비어있지 않으면 실제 리콜 데이터가 있다고 판단
    public boolean hasRecallData() {
        return resultData != null && !resultData.isEmpty();
    }
}