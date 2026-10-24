package com.photoconnect.service;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String otpCode, String purposeDescription);

    void sendPasswordResetEmail(String toEmail, String otpCode);

    void sendEmailChangeVerification(String toEmail, String otpCode);

    void sendGeneralEmail(String toEmail, String subject, String body);

    boolean isCaptureMode();

    List<CapturedEmail> getCapturedEmails();

    CapturedEmail getLastCapturedEmail(String toEmail);

    void clearCapturedEmails();

    record CapturedEmail(String to, String subject, String body, LocalDateTime sentAt) {
    }
}
