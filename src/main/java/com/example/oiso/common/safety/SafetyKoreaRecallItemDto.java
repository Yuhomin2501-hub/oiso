package com.example.oiso.common.safety;     // 세이프티코리아 API 관련 클래스들이 들어있는 패키지

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)     // API 응답 JSON에 DTO에 없는 필드가 있어도 오류가 나지 않게 무시
public class SafetyKoreaRecallItemDto {         // safety korea api 응답 안에 있는 리콜상품 1개의 상세 정보를 담는 DTO

    private String recallUid;           // 리콜 상품의 고유 ID
    private String recallProductName;   // 리콜 제품명
    private String recallBrandName;     // 리콜 브랜드명
    private String recallModelName;     // 리콜 모델명
    private String recallTypeName;      // 리콜 유형 또는 리콜 종류
    private String recallCmpnyName;     // 리콜 업체명
    private String makerName;           // 제조사명
    private String publishDate;         // 리콜 공표일
    private String harmDscr;            // 위해 내용 설명
    private String accidentCaseDscr;    // 사고 사례 설명
    private String publishActionDscr;   // 조치 내용 설명

    public SafetyKoreaRecallItemDto() {      // ObjectMapper가 JSON을 DTO로 변환할 때 사용하는 기본 생성자
    }

    public String getRecallUid() {
        return recallUid;
    }
    // 리콜 고유 ID 반환

    public void setRecallUid(String recallUid) {
        this.recallUid = recallUid;
    }
    // JSON에서 받은 리콜 고유 ID 저장

    public String getRecallProductName() {
        return recallProductName;
    }
    // 리콜 제품명 반환

    public void setRecallProductName(String recallProductName) {
        this.recallProductName = recallProductName;
    }
    // JSON에서 받은 리콜 제품명 저장

    public String getRecallBrandName() {
        return recallBrandName;
    }
    // 리콜 브랜드명 반환

    public void setRecallBrandName(String recallBrandName) {
        this.recallBrandName = recallBrandName;
    }
    // JSON에서 받은 리콜 브랜드명 저장

    public String getRecallModelName() {
        return recallModelName;
    }
    // 리콜 모델명 반환

    public void setRecallModelName(String recallModelName) {
        this.recallModelName = recallModelName;
    }
    // JSON에서 받은 리콜 모델명 저장

    public String getRecallTypeName() {
        return recallTypeName;
    }
    // 리콜 유형 반환

    public void setRecallTypeName(String recallTypeName) {
        this.recallTypeName = recallTypeName;
    }
    // JSON에서 받은 리콜 유형 저장

    public String getRecallCmpnyName() {
        return recallCmpnyName;
    }
    // 리콜 업체명 반환

    public void setRecallCmpnyName(String recallCmpnyName) {
        this.recallCmpnyName = recallCmpnyName;
    }
    // JSON에서 받은 리콜 업체명 저장

    public String getMakerName() {
        return makerName;
    }
    // 제조사명 반환

    public void setMakerName(String makerName) {
        this.makerName = makerName;
    }
    // JSON에서 받은 제조사명 저장

    public String getPublishDate() {
        return publishDate;
    }
    // 리콜 공표일 반환

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
    // JSON에서 받은 리콜 공표일 저장

    public String getHarmDscr() {
        return harmDscr;
    }
    // 위해 내용 설명 반환

    public void setHarmDscr(String harmDscr) {
        this.harmDscr = harmDscr;
    }
    // JSON에서 받은 위해 내용 설명 저장

    public String getAccidentCaseDscr() {
        return accidentCaseDscr;
    }
    // 사고 사례 설명 반환

    public void setAccidentCaseDscr(String accidentCaseDscr) {
        this.accidentCaseDscr = accidentCaseDscr;
    }
    // JSON에서 받은 사고 사례 설명 저장

    public String getPublishActionDscr() {
        return publishActionDscr;
    }
    // 조치 내용 설명 반환

    public void setPublishActionDscr(String publishActionDscr) {
        this.publishActionDscr = publishActionDscr;
    }
    // JSON에서 받은 조치 내용 설명 저장
}