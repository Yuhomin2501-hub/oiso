package com.example.oiso.chat.controller;   //채팅방 관련 요청을 처리함

import com.example.oiso.chat.dto.ChatRoomResponseDto;
import com.example.oiso.chat.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;  //채팅방 관련 실제 로직은 Service에 위임

    public ChatRoomController(ChatRoomService chatRoomService) {    //생성자 주입방식으로 ChatRoomService 주입
        this.chatRoomService = chatRoomService; //주입받은 서비스를 필드에 저장
    }

    @GetMapping("/my-list")
    @ResponseBody
    public String getMyChatRoomList(HttpSession session) {  //채팅방목록을 문자열 형태로 조회
        Object loginUserEmail = session.getAttribute("loginUserEmail"); //세션에서 로그인한 사용자의 이메일을 가져옴

        if (loginUserEmail == null) {   //세션에 로그인한 정보가 없으면 로그인하지 않은 상태로 판단
            return "로그인 후 이용 가능합니다.";   //안내 메시지 반환
        }

        return chatRoomService.getMyChatRoomList(loginUserEmail.toString()); //사용자 이메일을 기준으로 Service에 채팅방목록조회 요청
    }

    @GetMapping("/my-list-data")
    @ResponseBody
    public List<ChatRoomResponseDto> getMyChatRoomListData(HttpSession session) { //프론트에서 채팅방목록을 카드형태로 출력하기위한 데이터조회
        Object loginUserEmail = session.getAttribute("loginUserEmail"); //세션에서 로그인한 사용자이메일 가져옴

        if (loginUserEmail == null) { //로그인 상태가 아닌경우
            return null; //채팅방 데이터를 보내지 않음
        }

        return chatRoomService.getMyChatRoomListData(loginUserEmail.toString()); // 사용자이메일을 기준으로 채팅방목록 DTO리스트 반환
    }
}