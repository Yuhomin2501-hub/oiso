package com.example.oiso.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_email", length = 300, nullable = false)
    private String userEmail;

    @Column(name = "user_pwd", length = 100, nullable = false)
    private String userPwd;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "user_pnum", length = 20, nullable = false)
    private String userPnum;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public User(String userEmail, String userPwd, String userName, String userPnum, LocalDateTime regDt) {
        this.userEmail = userEmail;
        this.userPwd = userPwd;
        this.userName = userName;
        this.userPnum = userPnum;
        this.regDt = regDt;
    }

    public void changePassword(String userPwd) {
        this.userPwd = userPwd;
    }

    public void changeUserName(String userName) {
        this.userName = userName;
    }

    public void changeUserPnum(String userPnum) {
        this.userPnum = userPnum;
    }
}