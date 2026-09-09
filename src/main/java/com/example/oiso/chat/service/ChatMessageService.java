package com.example.oiso.chat.service;

import com.example.oiso.chat.document.ChatMessage;
import com.example.oiso.chat.document.ChatRoom;
import com.example.oiso.chat.dto.ChatMessageResponseDto;
import com.example.oiso.chat.repository.ChatMessageRepository;
import com.example.oiso.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository; // 채팅 메시지 저장/조회 담당
    private final ChatRoomRepository chatRoomRepository; // 채팅방 조회 담당

    public ChatMessageService(ChatMessageRepository chatMessageRepository, // 채팅 메시지 Repository 주입
                              ChatRoomRepository chatRoomRepository) { // 채팅방 Repository 주입
        this.chatMessageRepository = chatMessageRepository; // 주입받은 ChatMessageRepository 저장
        this.chatRoomRepository = chatRoomRepository; // 주입받은 ChatRoomRepository 저장
    }


    public String sendMessage(String chatRoomId, String loginUserEmail, String message) { // 채팅 메시지 전송 기능
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId); // 채팅방 ID로 채팅방 조회

        if (optionalChatRoom.isEmpty()) { // 해당 채팅방이 존재하지 않으면
            return "해당 채팅방이 없습니다."; // 채팅방 없음 메시지 반환
        }

        ChatRoom chatRoom = optionalChatRoom.get(); // Optional에서 ChatRoom 객체 꺼내기

        boolean isParticipant = // 현재 로그인 사용자가 채팅방 참여자인지 확인
                loginUserEmail.equals(chatRoom.getSellerEmail()) || // 로그인 사용자가 판매자이면 참여자
                        loginUserEmail.equals(chatRoom.getBuyerEmail()); // 로그인 사용자가 구매자이면 참여자

        if (!isParticipant) { // 채팅방 참여자가 아니라면
            return "해당 채팅방 참여자만 메시지를 보낼 수 있습니다."; // 메시지 전송 차단
        }

        if (message == null || message.trim().isEmpty()) { // 메시지 내용이 없거나 공백뿐이면
            return "메시지 내용은 필수입니다."; // 메시지 입력 요청
        }

        ChatMessage chatMessage = new ChatMessage( // 새 채팅 메시지 Document 생성
                chatRoomId, // 메시지가 속한 채팅방 ID
                loginUserEmail, // 메시지를 보낸 사용자 이메일
                message.trim(), // 앞뒤 공백을 제거한 메시지 내용
                LocalDateTime.now() // 메시지 전송 시간
        );

        chatMessageRepository.save(chatMessage); // 채팅 메시지를 MongoDB에 저장

        return "메시지 전송 완료"; // 메시지 전송 성공 메시지 반환
    }

    public String getChatMessageList(String chatRoomId, String loginUserEmail) { // 문자열 형태의 채팅 메시지 목록 조회 기능
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId); // 채팅방 ID로 채팅방 조회

        if (optionalChatRoom.isEmpty()) { // 해당 채팅방이 존재하지 않으면
            return "해당 채팅방이 없습니다."; // 채팅방 없음 메시지 반환
        }

        ChatRoom chatRoom = optionalChatRoom.get(); // Optional에서 ChatRoom 객체 꺼내기

        boolean isParticipant = // 현재 로그인 사용자가 채팅방 참여자인지 확인
                loginUserEmail.equals(chatRoom.getSellerEmail()) || // 로그인 사용자가 판매자이면 참여자
                        loginUserEmail.equals(chatRoom.getBuyerEmail()); // 로그인 사용자가 구매자이면 참여자

        if (!isParticipant) { // 채팅방 참여자가 아니라면
            return "해당 채팅방 참여자만 메시지를 조회할 수 있습니다."; // 메시지 조회 차단
        }

        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId); // 해당 채팅방 메시지를 보낸 시간 오름차순으로 조회

        if (chatMessageList.isEmpty()) { // 채팅 메시지가 하나도 없으면
            return "채팅 메시지가 없습니다."; // 메시지 없음 반환
        }

        StringBuilder result = new StringBuilder(); // 문자열 응답을 만들기 위한 객체

        for (ChatMessage chatMessage : chatMessageList) { // 조회된 채팅 메시지들을 하나씩 반복
            result.append("보낸사람: ").append(chatMessage.getSenderEmail()).append("\n"); // 보낸 사람 이메일 추가
            result.append("메시지내용: ").append(chatMessage.getMessage()).append("\n"); // 메시지 내용 추가
            result.append("보낸시간: ").append(chatMessage.getSentAt()).append("\n"); // 메시지 전송 시간 추가
            result.append("--------------------").append("\n"); // 메시지 구분선 추가
        }

        return result.toString(); // 완성된 채팅 메시지 문자열 반환
    }

    public List<ChatMessageResponseDto> getChatMessageListData(String chatRoomId, String loginUserEmail) { // 프론트용 DTO 형태의 채팅 메시지 목록 조회
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId); // 채팅방 ID로 채팅방 조회

        if (optionalChatRoom.isEmpty()) { // 채팅방이 존재하지 않으면
            return null; // null 반환
        }

        ChatRoom chatRoom = optionalChatRoom.get(); // Optional에서 ChatRoom 객체 꺼내기

        boolean isParticipant = // 현재 로그인 사용자가 채팅방 참여자인지 확인
                loginUserEmail.equals(chatRoom.getSellerEmail()) || // 로그인 사용자가 판매자이면 참여자
                        loginUserEmail.equals(chatRoom.getBuyerEmail()); // 로그인 사용자가 구매자이면 참여자

        if (!isParticipant) { // 채팅방 참여자가 아니라면
            return null; // 조회 권한이 없으므로 null 반환
        }

        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId); // 해당 채팅방 메시지를 시간순으로 조회
        List<ChatMessageResponseDto> result = new ArrayList<>(); // 프론트로 보낼 DTO 리스트 생성

        for (ChatMessage chatMessage : chatMessageList) { // 채팅 메시지 목록 반복
            result.add(new ChatMessageResponseDto( // ChatMessage Document를 ChatMessageResponseDto로 변환해서 추가
                    chatMessage.getChatRoomId(), // 채팅방 ID
                    chatMessage.getSenderEmail(), // 보낸 사람 이메일
                    chatMessage.getMessage(), // 메시지 내용
                    chatMessage.getSentAt(), // 보낸 시간
                    loginUserEmail.equals(chatMessage.getSenderEmail()) // 현재 로그인 사용자가 보낸 메시지인지 여부
            ));
        }

        return result; // 프론트용 채팅 메시지 DTO 리스트 반환
    }
}