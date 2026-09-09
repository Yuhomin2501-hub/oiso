package com.example.oiso.chat.document; // mongodb에 저장되는 채팅방 document 클래스가 위치

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_room") //이 클래스 객체는 mongodb의 chat_room컬렉션에 저장됨
@Getter
@NoArgsConstructor // mongodb가 데이터를 객체로 변환시 기본생성자를 자동생성함
public class ChatRoom {

    @Id
    private String id; //MongoDB에서 자동 생성되는 채팅방 고유 ID

    private Integer productId; // 교환대상이 되는 상대방 상품의 ID

    private String sellerEmail; // 상품을 등록한 판매자 이메일

    private String buyerEmail; // 교환을 신청한 구매자 또는 요청자 이메일

    private LocalDateTime createdAt; // 채팅방이 생성된 시간

    //채팅방을 새로 생성할 때 사용하는 생성자
    public ChatRoom(Integer productId, String sellerEmail, String buyerEmail, LocalDateTime createdAt) {
        this.productId = productId; // 교환 대상 상품 ID저장
        this.sellerEmail = sellerEmail; // 판매자 이메일 저장
        this.buyerEmail = buyerEmail; // 교환 신청지 이메일 저장
        this.createdAt = createdAt; // 채팅방 생성 시간 저장
    }
}