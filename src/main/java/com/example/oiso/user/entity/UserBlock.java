package com.example.oiso.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_block")
@Getter
@NoArgsConstructor
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id", nullable = false)
    private Integer blockId;

    @Column(name = "blocker_email", nullable = false, length = 300)
    private String blockerEmail;

    @Column(name = "blocked_email", nullable = false, length = 300)
    private String blockedEmail;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    public UserBlock(String blockerEmail,
                     String blockedEmail,
                     LocalDateTime regDt) {
        this.blockerEmail = blockerEmail;
        this.blockedEmail = blockedEmail;
        this.regDt = regDt;
    }
}