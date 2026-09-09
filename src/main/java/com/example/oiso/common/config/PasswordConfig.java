package com.example.oiso.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// 비밀번호 암호화 설정 파일

// 회원가입시 비밀번호를 그대로 저장하면 위험하기 때문에 BCryptPasswordEncoder로 암호화해서 사용한다.
// UserService에서 쓰이며 사용자가 입력한 비밀번호를 암호화해서 DB에 저장한다.

// 회원비밀번호를 안전하게 암호화하고 검증하기 위한 설정파일