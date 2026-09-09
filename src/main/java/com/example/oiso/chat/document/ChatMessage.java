package com.example.oiso.chat.document; //mongodb에 저장되는 채팅메시지 document클래스가 위치하는 패키지

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message") // 이 클래스의 객체는 mongodb의 chat_message 컬렉션에 자동저장
@Getter
@NoArgsConstructor //mongodb가 데이터를 객체로 변환 할 때 필요한 기본생성자 자동 생성
public class ChatMessage {

    @Id
    private String id; // mongodb에서 자동생성되는 채팅메시지 고유 ID

    private String chatRoomId; // 이 메시지가 어느 채팅방에 속한 메시지인지 구분하는 채팅방 ID

    private String senderEmail; //에시지를 보낸 사용자의 이메일

    private String message; //사용자가 실제로 입력한 채팅 메시지 내용

    private LocalDateTime sentAt; // 메시지 전송시간

    //채팅메시지를 새로생성할때 사용하는 생성자
    public ChatMessage(String chatRoomId, String senderEmail, String message, LocalDateTime sentAt) {
        this.chatRoomId = chatRoomId; // 전달받은 채팅방 ID저장
        this.senderEmail = senderEmail; // 전달받은 발신자 이메일저장
        this.message = message; // 전달받은 메시지 내용 저장
        this.sentAt = sentAt; // 전달받은 메시지 전송시간 저장
    }
}