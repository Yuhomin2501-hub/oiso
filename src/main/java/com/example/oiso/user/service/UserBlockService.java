package com.example.oiso.user.service;

import com.example.oiso.user.entity.UserBlock;
import com.example.oiso.user.repository.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;

    public UserBlockService(UserBlockRepository userBlockRepository) {
        this.userBlockRepository = userBlockRepository;
    }

    // 사용자 차단
    public String blockUser(String blockerEmail, String blockedEmail) {

        if (blockerEmail.equals(blockedEmail)) {
            return "본인은 차단할 수 없습니다.";
        }

        if (userBlockRepository.existsByBlockerEmailAndBlockedEmail(blockerEmail, blockedEmail)) {
            return "이미 차단한 사용자입니다.";
        }

        UserBlock userBlock = new UserBlock(
                blockerEmail,
                blockedEmail,
                LocalDateTime.now()
        );

        userBlockRepository.save(userBlock);

        return "사용자 차단 완료";
    }

    // 사용자 차단 취소
    @Transactional
    public String unblockUser(String blockerEmail, String blockedEmail) {

        if (!userBlockRepository.existsByBlockerEmailAndBlockedEmail(blockerEmail, blockedEmail)) {
            return "차단하지 않은 사용자입니다.";
        }

        userBlockRepository.deleteByBlockerEmailAndBlockedEmail(blockerEmail, blockedEmail);

        return "사용자 차단 취소 완료";
    }

    // 차단 여부 확인
    public boolean isBlocked(String blockerEmail, String blockedEmail) {

        return userBlockRepository.existsByBlockerEmailAndBlockedEmail(
                blockerEmail,
                blockedEmail
        );
    }
}