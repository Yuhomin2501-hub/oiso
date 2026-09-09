package com.example.oiso.user.controller;

import com.example.oiso.user.dto.EmailCodeSendRequestDto;
import com.example.oiso.user.dto.EmailCodeVerifyRequestDto;
import com.example.oiso.user.dto.SignupRequestDto;
import com.example.oiso.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService UserService) { // 생성자 주입
        this.userService = UserService; // 주입받은 UserService를 필드에 저장
    }

    @GetMapping("/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam String userEmail) { // userEmail 파라미터를 받음
        return userService.checkEmail(userEmail); // 이메일 형식/중복 확인은 UserService에서 처리
    }

    @PostMapping("/send-email-code")
    @ResponseBody
    public String sendEmailCode(@RequestBody EmailCodeSendRequestDto requestDto) { // JSON body를 DTO로 받음
        return userService.sendEmailCode(requestDto.getUserEmail()); // 이메일 인증번호 발송 로직 호출
    }

    @PostMapping("/verify-email-code")
    @ResponseBody
    public String verifyEmailCode(@RequestBody EmailCodeVerifyRequestDto requestDto) { // 이메일과 인증번호를 DTO로 받음
        return userService.verifyEmailCode(requestDto.getUserEmail(), requestDto.getCode()); // 인증번호 일치 여부 확인
    }

    @PostMapping("/signup")
    @ResponseBody
    public String signup(@RequestBody SignupRequestDto requestDto) { // json으로 온 회원가입 정보를 SignupRequestDto 객체로 받음
        // 사용자가 입력한 회원 정보가 requestDto안에 담김
        return userService.signup(requestDto); // 회원가입 검증, 비밀번호 암호화, DB 저장은 UserService에서 처리
    }

    @PostMapping("/login") // post 방식의 /login요청을 처리함
    public String login(@RequestParam String userEmail, // 로그인할 이메일을 form 데이터로 받음
                        @RequestParam String userPwd, // 로그인할 비밀번호를 form 데이터로 받음
                        HttpSession session, // 로그인 성공 시 사용자 이메일을 저장할 세션 객체, 다른페이지에서 필요한 경우 꺼내서 사용자인증을 하게함
                        RedirectAttributes redirectAttributes) { // 로그인 실패 시 로그인페이지로 redirect 할 때 전달해라 , 로그인실패시 결과값을 로그인페이지로 전달하기위함
                        // redirect시 메시지나 상태값을 같이 넘기기위한 객체
        boolean loginResult = userService.login(userEmail, userPwd); // UserService에서 이메일/비밀번호 검증 후 true/false 반환
        // UserService.login 부분에서 UserService의 메서드 부분을 호출하게됨

        if (!loginResult) { // 로그인 실패
            redirectAttributes.addAttribute("loginError", "true"); // 로그인 페이지로 loginError=true 값을 넘김 ( login.html 179)
            return "redirect:/login.html"; // 로그인 페이지로 다시 이동
        }

        session.setAttribute("loginUserEmail", userEmail); // 로그인 성공 시 세션에 로그인한 사용자의 이메일 저장(인증처리핵심)
        return "redirect:/index.html"; // 로그인 성공 후 메인 페이지로 이동
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) { // 현재 사용자의 세션 객체를 받음
        session.invalidate(); // 세션 전체를 무효화해서 로그인 상태 해제
        return "redirect:/index.html"; // 로그아웃 후 메인 페이지로 이동
    }

    @GetMapping("/find-email")
    @ResponseBody
    public String findEmail(@RequestParam String userName, // 이름 파라미터
                            @RequestParam String userPnum) { // 전화번호 파라미터
        return userService.findEmail(userName, userPnum); // 이름과 전화번호로 가입 이메일 찾기
    }

    @GetMapping("/reset-password")
    @ResponseBody
    public String resetPassword(@RequestParam String userEmail, // 이메일 파라미터
                                @RequestParam String userName, // 이름 파라미터
                                @RequestParam String userPnum, // 전화번호 파라미터
                                @RequestParam String newUserPwd) { // 새 비밀번호 파라미터
        return userService.resetPassword(userEmail, userName, userPnum, newUserPwd); // 회원 확인 후 비밀번호 재설정
    }

    @GetMapping("/update-my-info")
    @ResponseBody // 문자열을 그대로 응답으로 반환
    public String updateMyInfo(@RequestParam(required = false) String userName, // 수정할 이름, 없을 수도 있음
                               @RequestParam(required = false) String userPnum, // 수정할 전화번호, 없을 수도 있음
                               @RequestParam(required = false) String newUserPwd, // 수정할 새 비밀번호, 없을 수도 있음
                               HttpSession session) { // 현재 로그인 사용자를 확인하기 위한 세션 객체

        Object loginUserEmail = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 꺼내기

        if (loginUserEmail == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            return "로그인 후 이용 가능합니다."; // 로그인 필요 메시지 반환
        }

        return userService.updateMyInfo( // 내 정보 수정 로직 호출
                loginUserEmail.toString(), // 세션에서 꺼낸 이메일을 기준으로 현재 사용자 식별
                userName, // 수정할 이름
                userPnum, // 수정할 전화번호
                newUserPwd // 수정할 새 비밀번호
        );
    }

    @GetMapping("/withdraw")
    @ResponseBody
    public String withdraw(HttpSession session) { // 현재 로그인 사용자를 확인하기 위한 세션 객체
        Object loginUserEmail = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 꺼내기

        if (loginUserEmail == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            return "로그인 후 이용 가능합니다."; // 로그인 필요 메시지 반환
        }

        String result = userService.withdrawUser(loginUserEmail.toString()); // 세션 이메일 기준으로 회원 탈퇴 처리

        if (result.contains("완료")) { // 탈퇴가 정상 완료된 경우
            session.invalidate(); // 탈퇴 후 세션 무효화, 로그인 상태 제거
        }

        return result; // 회원 탈퇴 결과 메시지 반환
    }

    @GetMapping("/me-data")
    @ResponseBody
    public Map<String, Object> meData(HttpSession session) { // 현재 로그인 상태 확인을 위한 세션 객체
        Map<String, Object> result = new HashMap<>(); // 로그인 안 된 경우 반환할 결과 Map 생성

        Object loginUserEmailObject = session.getAttribute("loginUserEmail"); // 세션에서 로그인한 사용자 이메일 조회

        if (loginUserEmailObject == null) { // 세션에 이메일이 없으면 로그인하지 않은 상태
            result.put("loggedIn", false); // 로그인 여부 false 저장
            return result; // 로그인 안 된 상태 응답 반환
        }

        return userService.getMyPageData(loginUserEmailObject.toString()); // 세션 이메일 기준으로 마이페이지 데이터 조회 후 반환
    }
}