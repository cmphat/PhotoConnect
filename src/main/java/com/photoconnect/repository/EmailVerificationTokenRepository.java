package com.photoconnect.repository;

import com.photoconnect.entity.EmailVerificationPurpose;
import com.photoconnect.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            String email, EmailVerificationPurpose purpose);

    Optional<EmailVerificationToken> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email, EmailVerificationPurpose purpose);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}
