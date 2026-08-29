package com.fithaven.repository;

import com.fithaven.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByPhoneAndVerifiedFalseOrderByExpiresAtDesc(String phone);
    void deleteByPhone(String phone);
}
