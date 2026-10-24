package com.photoconnect.service;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.entity.User;

public interface UserService {

    User registerUser(RegisterRequest request);

    User registerUser(RegisterRequest request, String clientIp);

    User verifyRegistrationOtp(String email, String otpCode);

    void resendRegistrationOtp(String email, String clientIp);

    void requestPasswordReset(String email, String clientIp);

    void resetPassword(String email, String otpCode, String newPassword, String confirmPassword);

    void requestEmailChange(Long userId, String newEmail, String clientIp);

    void confirmEmailChange(Long userId, String otpCode);

    void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);

    void deactivateAccount(Long userId);
}
