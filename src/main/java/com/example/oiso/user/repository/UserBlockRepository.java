package com.example.oiso.user.repository;

import com.example.oiso.user.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, Integer> {

    boolean existsByBlockerEmailAndBlockedEmail(String blockerEmail, String blockedEmail);

    void deleteByBlockerEmailAndBlockedEmail(String blockerEmail, String blockedEmail);

    List<UserBlock> findByBlockerEmail(String blockerEmail);
}