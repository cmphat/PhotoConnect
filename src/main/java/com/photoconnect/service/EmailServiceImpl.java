package com.photoconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean captureMode;
    private final String fromAddress;
    private final String fromName;
    private final String configuredUsername;

    private final List<CapturedEmail> capturedEmails = new CopyOnWriteArrayList<>();

    public EmailServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${photoconnect.mail.capture-mode:true}") boolean captureMode,
            @Value("${photoconnect.mail.from-address:noreply@photoconnect.vn}") String fromAddress,
            @Value("${photoconnect.mail.from-name:PhotoConnect}") String fromName,
            @Value("${spring.mail.username:}") String configuredUsername) {
        this.mailSenderProvider = mailSenderProvider;
        this.captureMode = captureMode;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.configuredUsername = configuredUsername != null ? configuredUsername.trim() : "";
    }

    @Override
    public void sendVerificationEmail(String toEmail, String otpCode, String purposeDescription) {
        String subject = "[PhotoConnect] Mã xác thực tài khoản của bạn";
        String body = String.format(
                "Xin chào,\n\n" +
                        "Mã xác thực 6 chữ số cho yêu cầu %s của bạn là: %s\n\n" +
                        "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                        "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.\n\n" +
                        "Trân trọng,\nĐội ngũ PhotoConnect",
                purposeDescription, otpCode);

        sendEmailInternal(toEmail, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        String subject = "[PhotoConnect] Khôi phục mật khẩu tài khoản";
        String body = String.format(
                "Xin chào,\n\n" +
                        "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản PhotoConnect của bạn.\n" +
                        "Mã xác thực của bạn là: %s\n\n" +
                        "Mã có hiệu lực trong 5 phút và tối đa 5 lần thử.\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, tài khoản của bạn vẫn an toàn và bạn có thể bỏ qua thư này.\n\n" +
                        "Trân trọng,\nĐội ngũ PhotoConnect",
                otpCode);

        sendEmailInternal(toEmail, subject, body);
    }

    @Override
    public void sendEmailChangeVerification(String toEmail, String otpCode) {
        String subject = "[PhotoConnect] Xác nhận thay đổi địa chỉ email";
        String body = String.format(
                "Xin chào,\n\n" +
                        "Mã xác thực để cập nhật email mới cho tài khoản PhotoConnect của bạn là: %s\n\n" +
                        "Mã có hiệu lực trong 5 phút. Vui lòng nhập mã để hoàn tất thay đổi email.\n\n" +
                        "Trân trọng,\nĐội ngũ PhotoConnect",
                otpCode);

        sendEmailInternal(toEmail, subject, body);
    }

    @Override
    public void sendGeneralEmail(String toEmail, String subject, String body) {
        sendEmailInternal(toEmail, subject, body);
    }

    private void sendEmailInternal(String toEmail, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        // Local capture mode or unconfigured SMTP
        if (captureMode || mailSender == null || configuredUsername.isEmpty()) {
            capturedEmails.add(new CapturedEmail(toEmail, subject, body, LocalDateTime.now()));
            // CRITICAL SECURITY REQUIREMENT: Never log the OTP or secret body contents
            log.info("Captured email to [{}] with subject [{}]. (Local capture store active)", toEmail, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Successfully sent email via SMTP to [{}] with subject [{}]", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to deliver email to [{}] via SMTP: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isCaptureMode() {
        return captureMode || mailSenderProvider.getIfAvailable() == null || configuredUsername.isEmpty();
    }

    @Override
    public List<CapturedEmail> getCapturedEmails() {
        return Collections.unmodifiableList(capturedEmails);
    }

    @Override
    public CapturedEmail getLastCapturedEmail(String toEmail) {
        for (int i = capturedEmails.size() - 1; i >= 0; i--) {
            CapturedEmail email = capturedEmails.get(i);
            if (email.to().equalsIgnoreCase(toEmail)) {
                return email;
            }
        }
        return null;
    }

    @Override
    public void clearCapturedEmails() {
        capturedEmails.clear();
    }
}
