package com.example.oiso.chat.dto; //채팅방 목록 응답 DTO

import java.time.LocalDateTime; //채팅방 생성 시간을 표기

public class ChatRoomResponseDto { // 채팅방목록 화면에 필요한 데이터를 프론트로 내려주기위한 DTO

    private String chatRoomId; //mongodb에 저장된 채팅방 고유 ID

    private Integer productId; //상대방 상품 ID
    private String productName; // 상대방 상품명
    private String productImagePath; // 상대방상품 대표이미지경로
    private String productStatus; //상대방 상품 상태

    private Integer requesterProductId; //교환 신청자가 선택한 내 상품 ID
    private String requesterProductName; // 교환 신청자가 선택한 내 상품명
    private String requesterProductImagePath; // 교환 신청자가 선택한 내상품이미지 경로

    private String sellerEmail; // 상대방 상품을 등록한 판매자 이메일
    private String buyerEmail; // 교환을 신청한 사용자 이메일

    private String otherUserEmail; //현재 로그인한 사용자기준 상대방 이메일
    private String otherUserName; // 현재 로그인한 사용자기준 상대방 이름

    private LocalDateTime createdAt; // 채팅방 생성 시간

    public ChatRoomResponseDto(String chatRoomId,
                               Integer productId,
                               String productName,
                               String productImagePath,
                               String productStatus,
                               Integer requesterProductId,
                               String requesterProductName,
                               String requesterProductImagePath,
                               String sellerEmail,
                               String buyerEmail,
                               String otherUserEmail,
                               String otherUserName,
                               LocalDateTime createdAt) { //채팅방목록 응답객체를 만들때 값을 받는 생성자
        this.chatRoomId = chatRoomId; // 채팅방 ID 저장
        this.productId = productId; //상대방 상품 ID 저장
        this.productName = productName; // 상대방 상품명 저장
        this.productImagePath = productImagePath; // 상대방 상품 이미지 경로 저장
        this.productStatus = productStatus; // 상대방 상품 상태 저장
        this.requesterProductId = requesterProductId; // 교환 신청자가 선택한 상품 ID 저장
        this.requesterProductName = requesterProductName; // 교환신청자가 선택한 상품명 저장
        this.requesterProductImagePath = requesterProductImagePath; //교환신청자가 선택한 상품이미지 경로
        this.sellerEmail = sellerEmail; //판매자 이메일 저장
        this.buyerEmail = buyerEmail; // 교환 신청자 이메일 저장
        this.otherUserEmail = otherUserEmail; // 현재 로그인사용자 기준 상대방 이메일 저장
        this.otherUserName = otherUserName; // 현재 로그인사용자 기준 상대방 이름 저장
        this.createdAt = createdAt;// 채팅방 생성 시간 저장
    }

    public String getChatRoomId() {
        return chatRoomId;
    } // 채팅방 ID 반환

    public Integer getProductId() {
        return productId;
    } // 상대방 상품 ID 반환

    public String getProductName() {
        return productName;
    } // 상대방 상품명 반환

    public String getProductImagePath() {
        return productImagePath;
    } // 상대방 상품 이미지경로 반환

    public String getProductStatus() {
        return productStatus;
    } // 상품 상태 반환

    // 교환신청자가 선택한 상품ID반환
    public Integer getRequesterProductId() {
        return requesterProductId;
    }

    public String getRequesterProductName() {
        return requesterProductName;
    }// 상품명 반환

    //교환 신청자가 선택한 상품이미지 경로 반환
    public String getRequesterProductImagePath() {
        return requesterProductImagePath;
    }

    public String getSellerEmail() {
        return sellerEmail;
    } // 판매자 이메일 반환

    public String getBuyerEmail() {
        return buyerEmail;
    }// 교환 신청자 이메일 반환

    //현재 로그인 사용자 기준 상대방 이메일 반환
    public String getOtherUserEmail() {
        return otherUserEmail;
    }

    //현재 로그인 사용자 기준 상대방 이름 반환
    public String getOtherUserName() {
        return otherUserName;
    }

    //채팅방 생성시간 반환
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}