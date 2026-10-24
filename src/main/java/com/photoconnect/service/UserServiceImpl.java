package com.photoconnect.service;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.entity.EmailVerificationPurpose;
import com.photoconnect.entity.EmailVerificationToken;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.*;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailOtpService emailOtpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailOtpService = emailOtpService;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        return registerUser(request, null);
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request, String clientIp) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);

        User user;
        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            if (existing.getStatus() == UserStatus.PENDING_VERIFICATION) {
                // Update credentials and re-dispatch verification
                existing.setFullName(request.getFullName().trim());
                existing.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
                existing.setPassword(passwordEncoder.encode(request.getPassword()));
                user = userRepository.save(existing);
            } else {
                throw new EmailAlreadyExistsException("Email is already registered");
            }
        } else {
            user = new User();
            user.setFullName(request.getFullName().trim());
            user.setEmail(normalizedEmail);
            user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.PENDING_VERIFICATION);
            user = userRepository.save(user);
        }

        // Generate and dispatch OTP challenge
        emailOtpService.generateOtp(normalizedEmail, EmailVerificationPurpose.REGISTRATION, null, clientIp);

        return user;
    }

    @Override
    @Transactional
    public User verifyRegistrationOtp(String email, String otpCode) {
        String normalizedEmail = email.trim().toLowerCase();

        // 1. Verify OTP atomically
        emailOtpService.verifyOtp(normalizedEmail, otpCode, EmailVerificationPurpose.REGISTRATION);

        // 2. Activate user
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("Activated account for verified user: [{}]", normalizedEmail);

        return savedUser;
    }

    @Override
    @Transactional
    public void resendRegistrationOtp(String email, String clientIp) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Tài khoản này đã được kích hoạt.");
        }

        emailOtpService.resendOtp(normalizedEmail, EmailVerificationPurpose.REGISTRATION, clientIp);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email, String clientIp) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        // Anti-enumeration defense: Return identically regardless of existence
        if (userOpt.isEmpty() || userOpt.get().getStatus() == UserStatus.BANNED) {
            log.info("Password reset requested for nonexistent or banned email: [{}]", normalizedEmail);
            return;
        }

        emailOtpService.generateOtp(normalizedEmail, EmailVerificationPurpose.PASSWORD_RESET, null, clientIp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Mật khẩu xác nhận không khớp.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải chứa ít nhất 8 ký tự.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // 1. Verify OTP atomically
        emailOtpService.verifyOtp(normalizedEmail, otpCode, EmailVerificationPurpose.PASSWORD_RESET);

        // 2. Update password
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Successfully reset password for user: [{}]", normalizedEmail);
    }

    @Override
    @Transactional
    public void requestEmailChange(Long userId, String newEmail, String clientIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String normalizedNewEmail = newEmail.trim().toLowerCase();
        if (normalizedNewEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email mới không được trùng với email hiện tại.");
        }

        if (userRepository.existsByEmail(normalizedNewEmail)) {
            throw new EmailAlreadyExistsException("Email này đã được sử dụng bởi tài khoản khác.");
        }

        emailOtpService.generateOtp(user.getEmail(), EmailVerificationPurpose.EMAIL_CHANGE, normalizedNewEmail, clientIp);
    }

    @Override
    @Transactional
    public void confirmEmailChange(Long userId, String otpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        EmailVerificationToken token = emailOtpService.verifyOtp(
                user.getEmail(), otpCode, EmailVerificationPurpose.EMAIL_CHANGE);

        String newEmail = token.getTargetValue();
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalStateException("Không tìm thấy địa chỉ email mới cần cập nhật.");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email này đã được sử dụng bởi tài khoản khác.");
        }

        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        userRepository.save(user);
        log.info("Updated email for user [{}]: [{}] -> [{}]", userId, oldEmail, newEmail);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Mật khẩu hiện tại không chính xác.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Mật khẩu xác nhận không khớp.");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải chứa ít nhất 8 ký tự.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Changed password for user [{}]", userId);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Deactivated account for user [{}]", userId);
    }
}
