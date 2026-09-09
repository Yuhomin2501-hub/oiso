package com.example.oiso.chat.dto; //채팅 메시지 응답 DTO

import java.time.LocalDateTime; //메시지 전송시간을 표현

public class ChatMessageResponseDto { // 채팅데이터를 프론트로 내려주기위한 응답 DTO 클래스

    private String chatRoomId; // 메시지가 속한 채팅방 ID
    private String senderEmail; // 메시지를 보낸 사용자의 이메일
    private String message; // 실제 채팅 메시지 내용
    private LocalDateTime sentAt; // 메시지가 전송된 시간
    private boolean myMessage; // 현재 로그인한 사용자가 보낸 메시지인지 구분하는 값

    public ChatMessageResponseDto(String chatRoomId,
                                  String senderEmail,
                                  String message,
                                  LocalDateTime sentAt,
                                  boolean myMessage) { // 채팅 메시지 응답객체 생성시 필요한값을 받음
        this.chatRoomId = chatRoomId; // 전달받은 채팅방 ID 저장
        this.senderEmail = senderEmail; // 전달받은 발신자 이메일 저장
        this.message = message; // 전달받은 메시지 내용저장
        this.sentAt = sentAt; // 전달받은 전송 시간 저장
        this.myMessage = myMessage; // 내가보낸 메시지인지 여부 저장
    }

    public String getChatRoomId() {
        return chatRoomId;
    } // 채팅방 ID 반환

    public String getSenderEmail() {
        return senderEmail;
    } // 메시지 발신자 이메일 반환

    public String getMessage() {
        return message;
    } // 메시지 내용 반환

    public LocalDateTime getSentAt() {
        return sentAt;
    } // 메시지 전송시간 반환

    public boolean isMyMessage() {
        return myMessage;
    } // 현재 로그인한 사용자가 보낸 메시지인지 여부 반환
}