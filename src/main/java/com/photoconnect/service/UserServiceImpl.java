package com.photoconnect.service;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.EmailAlreadyExistsException;
import com.photoconnect.exception.PasswordMismatchException;
import com.photoconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }
}
