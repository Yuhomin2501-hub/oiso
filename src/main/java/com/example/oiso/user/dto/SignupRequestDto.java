package com.example.oiso.user.dto; // 사용자 관련 DTO클래스가 들어있는 패키지

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {      // 회원가입 요청 데이터를 담는 DTO 클래스

    private String userEmail;       // 회원가입할 사용자 이메일
    private String userPwd;         // 사용자가 입력한 비밀번호
    private String userPwdCheck;    // 비밀번호 확인 값
    private String userName;        // 사용자 이름
    private String userPnum;        // 사용자 전화번호
}