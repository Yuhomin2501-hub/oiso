package com.example.oiso.user.controller;

import com.example.oiso.user.service.UserBlockService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blocks")
public class UserBlockController {

    private final UserBlockService userBlockService;

    public UserBlockController(UserBlockService userBlockService) {
        this.userBlockService = userBlockService;
    }

    // 사용자 차단
    @PostMapping("/add")
    public String blockUser(@RequestParam String blockedEmail,
                            HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return userBlockService.blockUser(
                loginUserEmail.toString(),
                blockedEmail
        );
    }

    // 사용자 차단 취소
    @PostMapping("/delete")
    public String unblockUser(@RequestParam String blockedEmail,
                              HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return "로그인 후 이용 가능합니다.";
        }

        return userBlockService.unblockUser(
                loginUserEmail.toString(),
                blockedEmail
        );
    }

    // 차단 여부 확인
    @GetMapping("/status")
    public boolean isBlocked(@RequestParam String blockedEmail,
                             HttpSession session) {

        Object loginUserEmail = session.getAttribute("loginUserEmail");

        if (loginUserEmail == null) {
            return false;
        }

        return userBlockService.isBlocked(
                loginUserEmail.toString(),
                blockedEmail
        );
    }
}