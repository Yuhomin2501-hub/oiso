package com.example.oiso.exchange.controller;

import com.example.oiso.exchange.dto.ExchangeResponseDto;
import com.example.oiso.exchange.entity.Exchange;
import com.example.oiso.exchange.service.ExchangeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/exchanges/request") // 교환 신청 요청을 처리하는 api
    public String requestExchange(@RequestParam Integer productId, // 교환 대상 상품 ID
                                  @RequestParam Integer requesterProductId, // 교환 신청자가 제시하는 본인 상품 ID
                                  @RequestParam String requestMsg, // 교환 신청 메시지
                                  HttpSession session) {   // 현재 로그인한 사용자를 확인하기 위한 세션 객체
        String loginUserEmail = (String) session.getAttribute("loginUserEmail"); // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) { // 로그인 정보가 없을시
            return "로그인이 필요합니다.";
        }

        try {                   // 교환 신청 로직을 Service에 위임
            exchangeService.requestExchange(loginUserEmail, productId, requesterProductId, requestMsg);
            return "교환 신청 완료";
        } catch (IllegalArgumentException e) {      // Service에서 잘못된 요청이라고 판단한 경우
            return e.getMessage();      // 예외 메시지를 그대로 반환
        }
    }

    @GetMapping("/exchanges/received")      // 내가 받은 교환 신청 목록 조회 API
    public Object getReceivedExchangeList(HttpSession session) {        // 문자열 또는 교환 목록을 반환하기 위해 Object 사용
        String loginUserEmail = (String) session.getAttribute("loginUserEmail"); // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {       // 로그인하지 않은 경우
            return "로그인이 필요합니다.";
        }

        try {                             // 내가 받은 교환 신청 목록을 Entity 형태로 조회
            List<Exchange> exchangeList = exchangeService.getReceivedExchangeList(loginUserEmail);
            return exchangeList;        // 조회된 교환 신청 목록 반환
        } catch (IllegalArgumentException e) {      // 조회 중 잘못된 요청이나 예외 발생 시
            return e.getMessage();      // 예외 메시지 반환
        }
    }

    @GetMapping("/exchanges/received-data")         // 내가 받은 교환 신청 목록을 화면 출력용 DTO로 조회하는 API
    public Object getReceivedExchangeListData(HttpSession session) {      // 문자열 또는 DTO 목록을 반환하기 위해 Object 사용
        String loginUserEmail = (String) session.getAttribute("loginUserEmail");    // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {       // 로그인하지 않은 경우
            return "로그인이 필요합니다.";
        }

        try {                                        // 받은 교환 신청 목록을 DTO 형태로 조회
            List<ExchangeResponseDto> exchangeList = exchangeService.getReceivedExchangeListData(loginUserEmail);
            return exchangeList;    // 화면에 필요한 데이터만 담긴 DTO 목록 반환
        } catch (IllegalArgumentException e) {  // 조회 중 예외 발생 시
            return e.getMessage();
        }
    }

    @GetMapping("/exchanges/requested")     // 내가 보낸 교환 신청 목록 조회 API
    public Object getRequestedExchangeList(HttpSession session) {  // 문자열 또는 교환 목록을 반환하기 위해 Object 사용
        String loginUserEmail = (String) session.getAttribute("loginUserEmail");  // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {
            return "로그인이 필요합니다.";
        }

        try {
            List<Exchange> exchangeList = exchangeService.getRequestedExchangeList(loginUserEmail); // 내가 보낸 교환 신청 목록을 Entity 형태로 조회
            return exchangeList;   // 조회된 교환 신청 목록 반환
        } catch (IllegalArgumentException e) {   // 조회 중 예외 발생 시
            return e.getMessage();
        }
    }

    @GetMapping("/exchanges/requested-data")   // 내가 보낸 교환 신청 목록을 화면 출력용 DTO로 조회하는 API
    public Object getRequestedExchangeListData(HttpSession session) {   // 문자열 또는 DTO 목록을 반환하기 위해 Object 사용
        String loginUserEmail = (String) session.getAttribute("loginUserEmail");  // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {
            return "로그인이 필요합니다.";
        }

        try {                                        // 보낸 교환 신청 목록을 DTO 형태로 조회
            List<ExchangeResponseDto> exchangeList = exchangeService.getRequestedExchangeListData(loginUserEmail);
            return exchangeList;   // 화면에 필요한 데이터만 담긴 DTO 목록 반환
        } catch (IllegalArgumentException e) {  // 조회 중 예외 발생 시
            return e.getMessage();
        }
    }

    @GetMapping("/exchanges/status")  // 교환 신청 상태를 변경하는 API
    public String updateExchangeStatus(@RequestParam Integer exchangeId,   // 상태를 변경할 교환 신청 ID
                                       @RequestParam String exchangeStatus,  // 변경할 교환 상태 값
                                       HttpSession session) {  // 현재 로그인한 사용자를 확인하기 위한 세션 객체
        String loginUserEmail = (String) session.getAttribute("loginUserEmail");  // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {
            return "로그인이 필요합니다.";
        }

        try {
            exchangeService.updateExchangeStatus(exchangeId, loginUserEmail, exchangeStatus); // 교환 신청 상태 변경 로직을 Service에 위임
            return "교환 신청 상태 변경 완료";  // 정상 처리 시
        } catch (IllegalArgumentException e) {  // 권한이 없거나 잘못된 상태값인 경우
            return e.getMessage();  // 예외 메시지 반환
        }
    }

    @GetMapping("/exchanges/complete")  // 교환 거래를 완료 처리하는 API
    public String completeExchange(@RequestParam Integer exchangeId,  // 완료 처리할 교환 신청 ID
                                   HttpSession session) {  // 현재 로그인한 사용자를 확인하기 위한 세션 객체
        String loginUserEmail = (String) session.getAttribute("loginUserEmail"); // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {
            return "로그인이 필요합니다.";
        }

        try {
            exchangeService.completeExchange(exchangeId, loginUserEmail); // 거래 완료 처리 로직을 Service에 위임
            return "거래 완료 처리 성공";  // 정상 처리 시
        } catch (IllegalArgumentException e) {   // 권한이 없거나 처리할 수 없는 거래인 경우
            return e.getMessage();  // 예외 메시지 반환
        }
    }

    @GetMapping("/exchanges/cancel")  // 교환 거래를 취소 처리하는 API
    public String cancelExchange(@RequestParam Integer exchangeId,  // 취소 처리할 교환 신청 ID
                                 HttpSession session) {   // 현재 로그인한 사용자를 확인하기 위한 세션 객체
        String loginUserEmail = (String) session.getAttribute("loginUserEmail");  // 세션에서 로그인 사용자 이메일 조회

        if (loginUserEmail == null) {
            return "로그인이 필요합니다.";
        }

        try {
            exchangeService.cancelExchange(exchangeId, loginUserEmail);   // 거래 취소 처리 로직을 Service에 위임
            return "거래 취소 완료";  // 정상 처리 시
        } catch (IllegalArgumentException e) {  // 권한이 없거나 취소할 수 없는 거래인 경우
            return e.getMessage();  // 예외 메시지 반환
        }
    }
}