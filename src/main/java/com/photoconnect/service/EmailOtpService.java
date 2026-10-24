package com.photoconnect.service;

import com.photoconnect.entity.EmailVerificationPurpose;
import com.photoconnect.entity.EmailVerificationToken;

public interface EmailOtpService {

    String generateOtp(String email, EmailVerificationPurpose purpose, String targetValue, String clientIp);

    EmailVerificationToken verifyOtp(String email, String otpCode, EmailVerificationPurpose purpose);

    String resendOtp(String email, EmailVerificationPurpose purpose, String clientIp);
}
