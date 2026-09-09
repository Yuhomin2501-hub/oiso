package com.example.oiso.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailCodeVerifyRequestDto {        // 이메일 인증코드 검증 요청 데이터를 담는 DTO 클래스

    private String userEmail;       // 인증코드를 검증할 사용자 이메일
    private String code;            // 사용자가 입력한 인증코드
}




// 이메일 인증코드 검증 요청을 받을 때 사용하는 요청 DTO