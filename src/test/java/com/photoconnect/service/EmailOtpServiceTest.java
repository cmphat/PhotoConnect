package com.photoconnect.service;

import com.photoconnect.entity.EmailVerificationPurpose;
import com.photoconnect.entity.EmailVerificationToken;
import com.photoconnect.exception.*;
import com.photoconnect.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOtpServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private PasswordEncoder passwordEncoder;
    private EmailOtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        otpService = new EmailOtpServiceImpl(tokenRepository, emailService, passwordEncoder);
    }

    @Test
    @DisplayName("generateOtp creates 6-digit numeric code preserving leading zeros and dispatches email")
    void generateOtp_success() {
        when(tokenRepository.countByEmailAndCreatedAtAfter(anyString(), any())).thenReturn(0L);
        when(tokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());

        String otp = otpService.generateOtp("user@example.com", EmailVerificationPurpose.REGISTRATION, null, "127.0.0.1");

        assertThat(otp).isNotNull().matches("^[0-9]{6}$");

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());

        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getPurpose()).isEqualTo(EmailVerificationPurpose.REGISTRATION);
        assertThat(saved.getMaxAttempts()).isEqualTo(5);
        assertThat(saved.getAttemptCount()).isEqualTo(0);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(4));

        // Verify passwordEncoder matches the generated OTP against the saved hash
        assertThat(passwordEncoder.matches(otp, saved.getTokenHash())).isTrue();

        verify(emailService).sendVerificationEmail(eq("user@example.com"), eq(otp), anyString());
    }

    @Test
    @DisplayName("generateOtp enforces 60-second cooldown")
    void generateOtp_enforcesCooldown() {
        when(tokenRepository.countByEmailAndCreatedAtAfter(anyString(), any())).thenReturn(1L);

        EmailVerificationToken recentToken = new EmailVerificationToken(
                "user@example.com", "hash", EmailVerificationPurpose.REGISTRATION, null, LocalDateTime.now().plusMinutes(5));
        recentToken.setCreatedAt(LocalDateTime.now().minusSeconds(20)); // only 20 seconds ago

        when(tokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc("user@example.com", EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(recentToken));

        assertThatThrownBy(() -> otpService.generateOtp("user@example.com", EmailVerificationPurpose.REGISTRATION, null, "127.0.0.1"))
                .isInstanceOf(OtpCooldownException.class)
                .hasMessageContaining("Vui lòng đợi");

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("generateOtp enforces hourly rate limit per email")
    void generateOtp_enforcesEmailRateLimit() {
        when(tokenRepository.countByEmailAndCreatedAtAfter(eq("spammer@example.com"), any())).thenReturn(10L);

        assertThatThrownBy(() -> otpService.generateOtp("spammer@example.com", EmailVerificationPurpose.REGISTRATION, null, "127.0.0.1"))
                .isInstanceOf(OtpRateLimitException.class)
                .hasMessageContaining("Quá nhiều yêu cầu");
    }

    @Test
    @DisplayName("verifyOtp consumes token on correct OTP")
    void verifyOtp_success() {
        String plainOtp = "123456";
        String hash = passwordEncoder.encode(plainOtp);

        EmailVerificationToken token = new EmailVerificationToken(
                "user@example.com", hash, EmailVerificationPurpose.REGISTRATION, null, LocalDateTime.now().plusMinutes(5));

        when(tokenRepository.findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc("user@example.com", EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailVerificationToken verified = otpService.verifyOtp("user@example.com", plainOtp, EmailVerificationPurpose.REGISTRATION);

        assertThat(verified.isUsed()).isTrue();
        assertThat(verified.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("verifyOtp increments attempts on wrong OTP and rejects")
    void verifyOtp_wrongOtp_incrementsAttempts() {
        String hash = passwordEncoder.encode("123456");

        EmailVerificationToken token = new EmailVerificationToken(
                "user@example.com", hash, EmailVerificationPurpose.REGISTRATION, null, LocalDateTime.now().plusMinutes(5));
        token.setAttemptCount(0);

        when(tokenRepository.findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc("user@example.com", EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "999999", EmailVerificationPurpose.REGISTRATION))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Bạn còn 4 lần thử");

        assertThat(token.getAttemptCount()).isEqualTo(1);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("verifyOtp rejects and invalidates token when max attempts reached")
    void verifyOtp_maxAttemptsReached() {
        String hash = passwordEncoder.encode("123456");

        EmailVerificationToken token = new EmailVerificationToken(
                "user@example.com", hash, EmailVerificationPurpose.REGISTRATION, null, LocalDateTime.now().plusMinutes(5));
        token.setAttemptCount(4);

        when(tokenRepository.findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc("user@example.com", EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "000000", EmailVerificationPurpose.REGISTRATION))
                .isInstanceOf(OtpMaxAttemptsException.class)
                .hasMessageContaining("đã bị vô hiệu hóa");

        assertThat(token.getAttemptCount()).isEqualTo(5);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("verifyOtp rejects expired token")
    void verifyOtp_expired() {
        String hash = passwordEncoder.encode("123456");

        EmailVerificationToken token = new EmailVerificationToken(
                "user@example.com", hash, EmailVerificationPurpose.REGISTRATION, null, LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc("user@example.com", EmailVerificationPurpose.REGISTRATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "123456", EmailVerificationPurpose.REGISTRATION))
                .isInstanceOf(OtpExpiredException.class)
                .hasMessageContaining("đã hết hạn");
    }
}
