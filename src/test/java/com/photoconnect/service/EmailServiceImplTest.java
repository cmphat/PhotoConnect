package com.photoconnect.service;

import com.photoconnect.exception.EmailDeliveryException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Test
    void smtpModeWithoutConfiguredUsernameFailsClosed() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mock(JavaMailSender.class));
        EmailServiceImpl service = new EmailServiceImpl(
                provider, false, "sender@example.com", "PhotoConnect", "");

        assertThrows(EmailDeliveryException.class,
                () -> service.sendVerificationEmail("recipient@example.com", "123456", "registration"));
        assertFalse(service.isCaptureMode());
        assertTrue(service.getCapturedEmails().isEmpty());
    }

    @Test
    void smtpTransportFailureIsPropagatedAndNeverCaptured() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        doThrow(new IllegalStateException("SMTP unavailable")).when(sender).send(any(org.springframework.mail.SimpleMailMessage.class));
        EmailServiceImpl service = new EmailServiceImpl(
                provider, false, "sender@example.com", "PhotoConnect", "sender@example.com");

        assertThrows(EmailDeliveryException.class,
                () -> service.sendVerificationEmail("recipient@example.com", "123456", "registration"));
        assertTrue(service.getCapturedEmails().isEmpty());
    }

    @Test
    void explicitCaptureModeStillCapturesWithoutSmtpCredentials() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        EmailServiceImpl service = new EmailServiceImpl(
                provider, true, "noreply@example.com", "PhotoConnect", "");

        service.sendVerificationEmail("recipient@example.com", "123456", "registration");

        assertTrue(service.isCaptureMode());
        assertNotNull(service.getLastCapturedEmail("recipient@example.com"));
    }
}
