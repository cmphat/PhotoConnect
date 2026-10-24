package com.photoconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRateLimiter Unit Tests")
class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    @DisplayName("Normal attempt is allowed initially")
    void checkAllowed_initiallyPermitted() {
        LoginRateLimiter.RateLimitResult result = rateLimiter.checkAllowed("user@example.com", "192.168.1.100");
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    @DisplayName("Email is locked out after 5 consecutive failures")
    void recordFailure_locksEmailAfter5Failures() {
        String email = "victim@example.com";
        String ip = "10.0.0.1";

        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure(email, ip);
            assertThat(rateLimiter.checkAllowed(email, ip).isAllowed()).isTrue();
        }

        // 5th failure triggers lockout
        rateLimiter.recordFailure(email, ip);

        LoginRateLimiter.RateLimitResult lockedResult = rateLimiter.checkAllowed(email, ip);
        assertThat(lockedResult.isAllowed()).isFalse();
        assertThat(lockedResult.errorMessage())
                .contains("Tài khoản tạm thời bị khóa do nhập sai mật khẩu quá 5 lần")
                .contains("phút");
    }

    @Test
    @DisplayName("Successful login clears failed count for email")
    void recordSuccess_clearsFailedAttempts() {
        String email = "recovery@example.com";
        String ip = "10.0.0.2";

        rateLimiter.recordFailure(email, ip);
        rateLimiter.recordFailure(email, ip);
        assertThat(rateLimiter.getFailedAttemptsForEmail(email)).isEqualTo(2);

        rateLimiter.recordSuccess(email, ip);
        assertThat(rateLimiter.getFailedAttemptsForEmail(email)).isEqualTo(0);
        assertThat(rateLimiter.checkAllowed(email, ip).isAllowed()).isTrue();
    }

    @Test
    @DisplayName("IP is locked out after 20 consecutive failures")
    void recordFailure_locksIpAfter20Failures() {
        String ip = "192.168.1.250";

        for (int i = 0; i < 19; i++) {
            rateLimiter.recordFailure("random" + i + "@example.com", ip);
            assertThat(rateLimiter.checkAllowed("fresh@example.com", ip).isAllowed()).isTrue();
        }

        // 20th failure triggers IP lockout
        rateLimiter.recordFailure("random20@example.com", ip);

        LoginRateLimiter.RateLimitResult lockedResult = rateLimiter.checkAllowed("fresh@example.com", ip);
        assertThat(lockedResult.isAllowed()).isFalse();
        assertThat(lockedResult.errorMessage())
                .contains("Địa chỉ IP của bạn bị tạm khóa")
                .contains("phút");
    }
}
