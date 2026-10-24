package com.photoconnect.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe rate limiter and brute-force protection for login authentication.
 * Enforces:
 * - Max 5 consecutive failed attempts per email within a 15-minute sliding window.
 * - Max 20 consecutive failed attempts per IP within a 15-minute sliding window.
 * - Automatic lockout clearance upon successful authentication or timeout expiry.
 */
@Component
public class LoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);

    public static final int MAX_FAILED_ATTEMPTS_PER_EMAIL = 5;
    public static final int MAX_FAILED_ATTEMPTS_PER_IP = 20;
    public static final int LOCKOUT_MINUTES = 15;

    private final Map<String, AttemptEntry> emailAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptEntry> ipAttempts = new ConcurrentHashMap<>();

    public record RateLimitResult(boolean isAllowed, String errorMessage) {
        public static RateLimitResult allowed() {
            return new RateLimitResult(true, null);
        }

        public static RateLimitResult locked(String message) {
            return new RateLimitResult(false, message);
        }
    }

    private static class AttemptEntry {
        int failedCount;
        LocalDateTime lockoutUntil;
        LocalDateTime firstAttemptAt;

        AttemptEntry() {
            this.failedCount = 0;
            this.firstAttemptAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if the given email and client IP are currently allowed to attempt login.
     */
    public RateLimitResult checkAllowed(String email, String clientIp) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check IP lockout
        if (clientIp != null && !clientIp.isBlank()) {
            AttemptEntry ipEntry = ipAttempts.get(clientIp);
            if (ipEntry != null && ipEntry.lockoutUntil != null) {
                if (now.isBefore(ipEntry.lockoutUntil)) {
                    long remainingMinutes = Math.max(1, Duration.between(now, ipEntry.lockoutUntil).toMinutes());
                    log.warn("Login attempt blocked for locked IP [{}]", clientIp);
                    return RateLimitResult.locked(
                            "Địa chỉ IP của bạn bị tạm khóa do có quá nhiều yêu cầu đăng nhập thất bại. Vui lòng thử lại sau " + remainingMinutes + " phút.");
                } else {
                    ipAttempts.remove(clientIp);
                }
            }
        }

        // 2. Check Email lockout
        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();
            AttemptEntry emailEntry = emailAttempts.get(normalizedEmail);
            if (emailEntry != null && emailEntry.lockoutUntil != null) {
                if (now.isBefore(emailEntry.lockoutUntil)) {
                    long remainingMinutes = Math.max(1, Duration.between(now, emailEntry.lockoutUntil).toMinutes());
                    log.warn("Login attempt blocked for locked email [{}]", normalizedEmail);
                    return RateLimitResult.locked(
                            "Tài khoản tạm thời bị khóa do nhập sai mật khẩu quá 5 lần. Vui lòng thử lại sau " + remainingMinutes + " phút hoặc sử dụng 'Quên mật khẩu'.");
                } else {
                    emailAttempts.remove(normalizedEmail);
                }
            }
        }

        return RateLimitResult.allowed();
    }

    /**
     * Records a failed login attempt for the given email and client IP.
     */
    public void recordFailure(String email, String clientIp) {
        LocalDateTime now = LocalDateTime.now();

        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();
            emailAttempts.compute(normalizedEmail, (k, entry) -> {
                if (entry == null || (entry.firstAttemptAt != null && Duration.between(entry.firstAttemptAt, now).toMinutes() > LOCKOUT_MINUTES)) {
                    entry = new AttemptEntry();
                }
                entry.failedCount++;
                if (entry.failedCount >= MAX_FAILED_ATTEMPTS_PER_EMAIL) {
                    entry.lockoutUntil = now.plusMinutes(LOCKOUT_MINUTES);
                    log.warn("Email [{}] locked out until [{}] after [{}] failed attempts", normalizedEmail, entry.lockoutUntil, entry.failedCount);
                }
                return entry;
            });
        }

        if (clientIp != null && !clientIp.isBlank()) {
            ipAttempts.compute(clientIp, (k, entry) -> {
                if (entry == null || (entry.firstAttemptAt != null && Duration.between(entry.firstAttemptAt, now).toMinutes() > LOCKOUT_MINUTES)) {
                    entry = new AttemptEntry();
                }
                entry.failedCount++;
                if (entry.failedCount >= MAX_FAILED_ATTEMPTS_PER_IP) {
                    entry.lockoutUntil = now.plusMinutes(LOCKOUT_MINUTES);
                    log.warn("IP [{}] locked out until [{}] after [{}] failed attempts", clientIp, entry.lockoutUntil, entry.failedCount);
                }
                return entry;
            });
        }
    }

    /**
     * Clears failed attempt counters upon successful authentication.
     */
    public void recordSuccess(String email, String clientIp) {
        if (email != null && !email.isBlank()) {
            emailAttempts.remove(email.trim().toLowerCase());
        }
        if (clientIp != null && !clientIp.isBlank()) {
            ipAttempts.remove(clientIp);
        }
    }

    /**
     * For administrative or testing purposes: reset all counters.
     */
    public void resetAll() {
        emailAttempts.clear();
        ipAttempts.clear();
    }

    public int getFailedAttemptsForEmail(String email) {
        if (email == null) return 0;
        AttemptEntry entry = emailAttempts.get(email.trim().toLowerCase());
        return entry != null ? entry.failedCount : 0;
    }
}
