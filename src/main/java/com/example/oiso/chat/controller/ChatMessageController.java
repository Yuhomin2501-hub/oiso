package com.example.oiso.chat.controller;

import com.example.oiso.chat.dto.ChatMessageResponseDto;
import com.example.oiso.chat.service.ChatMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/chat-messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService; // 채팅 메시지 관련 실제 처리는 Service에 위임

    public ChatMessageController(ChatMessageService chatMessageService) { // 생성자 주입
        this.chatMessageService = chatMessageService; // 주입받은 ChatMessageService를 필드에 저장
    }

    @GetMapping("/send")
    @ResponseBody
    public String sendMessage(@RequestParam String chatRoomId, // 메시지를 보낼 채팅방 ID를 받음
                              @RequestParam String message, // 사용자가 입력한 메시지 내용을 받음
                              HttpSession session) { // 현재 로그인 사용자를 확인하기 위한 세션 객체

        Object loginUserEmail = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 꺼내기

        if (loginUserEmail == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            return "로그인 후 이용 가능합니다."; // 로그인 필요 메시지 반환
        }

        return chatMessageService.sendMessage( // 채팅 메시지 전송 로직을 Service에 위임
                chatRoomId, // 채팅방 ID
                loginUserEmail.toString(), // 세션에서 꺼낸 현재 로그인 사용자 이메일
                message // 전송할 메시지 내용
        );
    }

    @GetMapping("/list")
    @ResponseBody
    public String getChatMessageList(@RequestParam String chatRoomId, // 조회할 채팅방 ID를 받음
                                     HttpSession session) { // 현재 로그인 사용자를 확인하기 위한 세션 객체

        Object loginUserEmail = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 꺼내기

        if (loginUserEmail == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            return "로그인 후 이용 가능합니다."; // 로그인 필요 메시지 반환
        }

        return chatMessageService.getChatMessageList( // 문자열 형태의 채팅 메시지 목록 조회를 Service에 위임
                chatRoomId, // 채팅방 ID
                loginUserEmail.toString() // 현재 로그인 사용자 이메일
        );
    }

    @GetMapping("/list-data")
    @ResponseBody
    public List<ChatMessageResponseDto> getChatMessageListData(@RequestParam String chatRoomId, // 조회할 채팅방 ID를 받음
                                                               HttpSession session) { // 현재 로그인 사용자를 확인하기 위한 세션 객체

        Object loginUserEmail = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 꺼내기

        if (loginUserEmail == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            return null; // 로그인하지 않은 경우 null 반환
        }

        return chatMessageService.getChatMessageListData( // 프론트에서 사용하기 좋은 DTO 형태의 메시지 목록 조회를 Service에 위임
                chatRoomId, // 채팅방 ID
                loginUserEmail.toString() // 현재 로그인 사용자 이메일
        );
    }
}