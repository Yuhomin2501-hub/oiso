package com.example.oiso.chat.repository; // 채팅 메시지 저장소

import com.example.oiso.chat.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 특정 채팅방 ID에 해당하는 메시지를 전송 시간 오름차순 조회
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(String chatRoomId);

    // 여러 채팅방 ID에 해당하는 메시지를 한번에 삭제
    void deleteByChatRoomIdIn(List<String> chatRoomIdList);
}