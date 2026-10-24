package com.photoconnect.service;

import com.photoconnect.entity.EmailVerificationPurpose;
import com.photoconnect.entity.EmailVerificationToken;
import com.photoconnect.exception.*;
import com.photoconnect.repository.EmailVerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EmailOtpServiceImpl implements EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpServiceImpl.class);

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_HOURLY_REQUESTS_PER_EMAIL = 10;
    private static final int MAX_HOURLY_REQUESTS_PER_IP = 25;

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    // IP rate limiter store: IP -> Pair of (Count, FirstRequestTimestamp)
    private final Map<String, IpRateLimitEntry> ipRateLimiter = new ConcurrentHashMap<>();

    public EmailOtpServiceImpl(
            EmailVerificationTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public String generateOtp(String email, EmailVerificationPurpose purpose, String targetValue, String clientIp) {
        String normalizedEmail = email.trim().toLowerCase();

        // 1. IP rate limiting
        enforceIpRateLimit(clientIp);

        // 2. Account rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = tokenRepository.countByEmailAndCreatedAtAfter(normalizedEmail, oneHourAgo);
        if (recentRequests >= MAX_HOURLY_REQUESTS_PER_EMAIL) {
            log.warn("Rate limit exceeded for email: [{}]", normalizedEmail);
            throw new OtpRateLimitException("Quá nhiều yêu cầu gửi mã xác thực. Vui lòng thử lại sau 1 giờ.");
        }

        // 3. 60-second cooldown check
        Optional<EmailVerificationToken> latestToken = tokenRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(normalizedEmail, purpose);

        if (latestToken.isPresent()) {
            LocalDateTime lastCreatedAt = latestToken.get().getCreatedAt();
            if (lastCreatedAt != null) {
                long secondsSince = Duration.between(lastCreatedAt, LocalDateTime.now()).getSeconds();
                if (secondsSince < COOLDOWN_SECONDS) {
                    long remainingWait = COOLDOWN_SECONDS - secondsSince;
                    throw new OtpCooldownException(
                            String.format("Vui lòng đợi %d giây trước khi yêu cầu mã xác thực mới.", remainingWait));
                }
            }
        }

        // 4. Generate 6-digit cryptographically secure code (preserving leading zeros)
        int randomInt = secureRandom.nextInt(1_000_000);
        String otpCode = String.format("%06d", randomInt);

        // 5. Hash token with salt via PasswordEncoder (no plaintext storage)
        String tokenHash = passwordEncoder.encode(otpCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        EmailVerificationToken token = new EmailVerificationToken(
                normalizedEmail,
                tokenHash,
                purpose,
                targetValue != null ? targetValue.trim().toLowerCase() : null,
                expiresAt);

        tokenRepository.save(token);

        // 6. Dispatch email notification (without logging OTP value)
        log.info("Dispatched OTP challenge for email: [{}] with purpose: [{}]", normalizedEmail, purpose);

        switch (purpose) {
            case REGISTRATION -> emailService.sendVerificationEmail(normalizedEmail, otpCode, "đăng ký tài khoản");
            case PASSWORD_RESET -> emailService.sendPasswordResetEmail(normalizedEmail, otpCode);
            case EMAIL_CHANGE -> emailService.sendEmailChangeVerification(
                    targetValue != null ? targetValue : normalizedEmail, otpCode);
        }

        return otpCode;
    }

    @Override
    @Transactional
    public EmailVerificationToken verifyOtp(String email, String otpCode, EmailVerificationPurpose purpose) {
        String normalizedEmail = email.trim().toLowerCase();

        Optional<EmailVerificationToken> tokenOpt = tokenRepository
                .findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(normalizedEmail, purpose);

        if (tokenOpt.isEmpty()) {
            throw new InvalidOtpException("Không tìm thấy mã xác thực đang hoạt động hoặc mã đã được sử dụng.");
        }

        EmailVerificationToken token = tokenOpt.get();

        // Check if expired
        if (token.isExpired()) {
            throw new OtpExpiredException("Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // Check attempts limit
        if (token.isAttemptsExceeded()) {
            throw new OtpMaxAttemptsException("Đã vượt quá số lần thử cho phép (5 lần). Mã xác thực đã bị vô hiệu hóa.");
        }

        // Compare OTP securely
        if (!passwordEncoder.matches(otpCode != null ? otpCode.trim() : "", token.getTokenHash())) {
            token.incrementAttemptCount();
            tokenRepository.save(token);

            int remainingAttempts = token.getMaxAttempts() - token.getAttemptCount();
            if (remainingAttempts <= 0) {
                log.warn("OTP max attempts exceeded for email: [{}]", normalizedEmail);
                throw new OtpMaxAttemptsException("Mã xác thực không chính xác. Bạn đã hết số lần thử. Mã xác thực đã bị vô hiệu hóa.");
            }
            throw new InvalidOtpException(
                    String.format("Mã xác thực không chính xác. Bạn còn %d lần thử.", remainingAttempts));
        }

        // Atomic consumption
        token.setUsedAt(LocalDateTime.now());
        EmailVerificationToken consumedToken = tokenRepository.save(token);
        log.info("Successfully verified and consumed OTP for email: [{}], purpose: [{}]", normalizedEmail, purpose);

        return consumedToken;
    }

    @Override
    @Transactional
    public String resendOtp(String email, EmailVerificationPurpose purpose, String clientIp) {
        // Fetch target value from previous token if purpose is EMAIL_CHANGE
        String targetValue = null;
        if (purpose == EmailVerificationPurpose.EMAIL_CHANGE) {
            Optional<EmailVerificationToken> prev = tokenRepository
                    .findTopByEmailAndPurposeOrderByCreatedAtDesc(email.trim().toLowerCase(), purpose);
            if (prev.isPresent()) {
                targetValue = prev.get().getTargetValue();
            }
        }
        return generateOtp(email, purpose, targetValue, clientIp);
    }

    private void enforceIpRateLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank() || "unknown".equalsIgnoreCase(clientIp)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ipRateLimiter.compute(clientIp, (ip, entry) -> {
            if (entry == null || Duration.between(entry.firstRequest(), now).toHours() >= 1) {
                return new IpRateLimitEntry(new AtomicInteger(1), now);
            }
            if (entry.count().incrementAndGet() > MAX_HOURLY_REQUESTS_PER_IP) {
                log.warn("IP rate limit exceeded for client IP: [{}]", clientIp);
                throw new OtpRateLimitException("Quá nhiều yêu cầu từ địa chỉ IP của bạn. Vui lòng thử lại sau.");
            }
            return entry;
        });
    }

    private record IpRateLimitEntry(AtomicInteger count, LocalDateTime firstRequest) {
    }
}
