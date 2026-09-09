package com.example.oiso.chat.repository; //채팅방 repository

import com.example.oiso.chat.document.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // 상품 ID, 판매자 이메일, 구매자 이메일이 모두 일치하는 채팅방을 조회
    Optional<ChatRoom> findByProductIdAndSellerEmailAndBuyerEmail(Integer productId, String sellerEmail, String buyerEmail);

    // 현재 사용자가 판매자이거나 구매자인 채팅방을 생성일 기준 최신순으로 조회
    List<ChatRoom> findBySellerEmailOrBuyerEmailOrderByCreatedAtDesc(String sellerEmail, String buyerEmail);
}