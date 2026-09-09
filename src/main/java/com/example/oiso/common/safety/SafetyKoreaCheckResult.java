package com.example.oiso.common.safety;

public class SafetyKoreaCheckResult {       // safety korea 검사결과를 담는 객체

    private final boolean blocked; // 상품등록,수정을 차단할지 여부
    private final String blockedKeyword; //차단원인을 구분하기 위한 키워드
    private final String blockedReason; //사용자 또는 로그에 남길 차단 상세 사유
    private final String recallUid; // safety korea 리콜 데이터의 고유 ID
    private final String recallProductName; //리콜 상품명
    private final String recallBrandName; // 리콜 브랜드명
    private final String recallModelName; // 리콜 모델명
    private final String recallTypeName; // 리콜 종류명
    private final String recallCompanyName; // 리콜 업체명
    private final String publishDate; // 리콜 공표일

    private SafetyKoreaCheckResult(boolean blocked,
                                   String blockedKeyword,
                                   String blockedReason,
                                   String recallUid,
                                   String recallProductName,
                                   String recallBrandName,
                                   String recallModelName,
                                   String recallTypeName,
                                   String recallCompanyName,
                                   String publishDate) { //외부에서 직접 생성하지 못하게 private생성자로 제한
        this.blocked = blocked; // 차단여부 저장
        this.blockedKeyword = blockedKeyword; // 차단 키워드 저장
        this.blockedReason = blockedReason; // 차단 사유 저장
        this.recallUid = recallUid; // 리콜 고유 ID저장
        this.recallProductName = recallProductName; // 리콜 상품명 저장
        this.recallBrandName = recallBrandName; // 리콜 브랜드명 저장
        this.recallModelName = recallModelName; // 리콜 모델명 저장
        this.recallTypeName = recallTypeName; // 리콜 종류명 저장
        this.recallCompanyName = recallCompanyName; // 리콜 업체명 저장
        this.publishDate = publishDate; // 리콜 공표일 저장
    }

    public static SafetyKoreaCheckResult pass() { // 검사결과 정상통과시 사용하는 정적 생성 메서드
        return new SafetyKoreaCheckResult(
                false, // 차단하지 않음
                null, // 차단 키워드 없음
                null, // 차단 사유 없음
                null, // 리콜 고유 ID 없음
                null, // 리콜 상품명 없음
                null, // 리콜 브랜드명 없음
                null, // 리콜 모델명 없음
                null, // 리콜 종류명 없음
                null, // 리콜 업체명 없음
                null // 리콜 공표일 없음
        );
    }

    public static SafetyKoreaCheckResult blocked(String recallUid,
                                                 String recallProductName,
                                                 String recallBrandName,
                                                 String recallModelName,
                                                 String recallTypeName,
                                                 String recallCompanyName,
                                                 String publishDate) { // 리콜 상품으로 확인되어 차단해야 할때 사용하는 정적 생성 메서드
        String reason = buildBlockedReason(
                recallProductName, // 차단 사유 문구에 사용할 리콜 상품명
                recallBrandName, // 차단 사유 문구에 사용할 브랜드명
                recallModelName, // 차단 사유 문구에 사용할 모델명
                recallTypeName, // 차단 사유 문구에 사용할 리콜 종류
                recallCompanyName, // 차단 사유 문구에 사용할 업체명
                publishDate // 차단 사유 문구에 사용할 공표일
        );

        return new SafetyKoreaCheckResult(
                true, // 차단처리
                "SAFETY_KOREA_RECALL", // safety korea 리콜로 인한 차단임을 나타내는 키워드
                reason, // 생성한 차단 사유 문구 저장
                recallUid, // 리콜 고유 ID 저장
                recallProductName, // 리콜 상품명 저장
                recallBrandName, // 리콜 브랜드명 저장
                recallModelName, // 리콜 모델명 저장
                recallTypeName, // 리콜 종류명 저장
                recallCompanyName, // 리콜 업체명 저장
                publishDate // 리콜 공표일 저장
        );
    }

    // safety korea api오류가 발생했을 때 임시로 통과 처리를 하는 정적 생성 메서드
    public static SafetyKoreaCheckResult apiErrorPass() {
        return new SafetyKoreaCheckResult(
                false, // api 오류만으로는 상품을 차단하지 않음
                null, // 차단 키워드 없음
                null, // 차단 사유 없음
                null, // 리콜 고유 ID 없음
                null, // 리콜 상품명 없음
                null, // 리콜 브랜드명 없음
                null, // 리콜 모델명 없음
                null, // 리콜 종류명 없음
                null, // 리콜 업체명 없음
                null // 리콜 공표일 없음
        );
    }

    // 리콜 정보를 이용해서 차단 사유 문장을 만드는 메서드
    private static String buildBlockedReason(String recallProductName,
                                             String recallBrandName,
                                             String recallModelName,
                                             String recallTypeName,
                                             String recallCompanyName,
                                             String publishDate) {
        StringBuilder reason = new StringBuilder(); // 문자열을 여러번 이어붙이기위해 사용
        reason.append("제품안전정보센터 국내리콜정보에서 리콜 상품으로 확인되어 등록이 차단되었습니다.");

        if (recallProductName != null && !recallProductName.isBlank()) { //리콜상품명이 비어있지 않은경우
            reason.append(" 제품명: ").append(recallProductName.trim()).append(".");
        }

        if (recallBrandName != null && !recallBrandName.isBlank()) {//브랜드명이 비어있지 않은 경우
            reason.append(" 브랜드: ").append(recallBrandName.trim()).append(".");
        }

        if (recallModelName != null && !recallModelName.isBlank()) {//모델명이 비어있지 않은 경우
            reason.append(" 모델명: ").append(recallModelName.trim()).append(".");
        }

        if (recallTypeName != null && !recallTypeName.isBlank()) {//리콜 종류가 비어있지 않은 경우
            reason.append(" 리콜종류: ").append(recallTypeName.trim()).append(".");
        }

        if (recallCompanyName != null && !recallCompanyName.isBlank()) {//업체명이 비어있지 않은 경우
            reason.append(" 업체명: ").append(recallCompanyName.trim()).append(".");
        }

        if (publishDate != null && !publishDate.isBlank()) {//공표일이 비어있지 않은 경우
            reason.append(" 공표일: ").append(publishDate.trim()).append(".");
        }

        return reason.toString(); // 완성된 차단 사유 문자열 반환
    }

    public boolean isBlocked() {
        return blocked;
    } //차단 여부를 반환하는 getter(true면 차단,false면 통과)

    public String getBlockedKeyword() {
        return blockedKeyword;
    } //차단키워드를 반환하는 getter

    public String getBlockedReason() {
        return blockedReason;
    } //차단 사유를 반환하는 getter

    public String getRecallUid() {
        return recallUid;
    } //리콜 고유 ID를 반환하는 getter

    public String getRecallProductName() {
        return recallProductName;
    }//리콜 상품명을 반환하는 getter

    public String getRecallBrandName() {
        return recallBrandName;
    }//리콜 브랜드명을 반환하는 getter

    public String getRecallModelName() {
        return recallModelName;
    }//리콜 모델명을 반환하는 getter

    public String getRecallTypeName() {
        return recallTypeName;
    }//리콜 종류명을 반환하는 getter

    public String getRecallCompanyName() {
        return recallCompanyName;
    }//리콜 업체명을 반환하는 getter

    public String getPublishDate() {
        return publishDate;
    }//리콜 공표일을 반환하는 getter

}