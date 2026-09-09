package com.example.oiso.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailCodeSendRequestDto {  // 이메일 인증코드 발송 요청 데이터를 담는 DTO 클래스

    private String userEmail; // 인증 코드를 받을 사용자 이메일
}