package com.example.oiso.user.repository;

import com.example.oiso.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// UserRepository가 JpaRepository<User, String> 를 상속 받고 있음
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUserEmail(String userEmail);

    Optional<User> findByUserEmail(String userEmail); // 이메일로 회원을 조회하는 메서드

    Optional<User> findByUserNameAndUserPnum(String userName, String userPnum); // 이름과 전화번호로 사용자 찾기

    Optional<User> findByUserEmailAndUserNameAndUserPnum(String userEmail, String userName, String userPnum);
    // 이메일,이름,전화번호로 사용자 찾기
}