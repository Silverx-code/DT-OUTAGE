package com.gridline.dtoutage.repository;

import com.gridline.dtoutage.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUser_UserId(UUID userId);
    void deleteByExpiresAtBefore(Instant now);
}
