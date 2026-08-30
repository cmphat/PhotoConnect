package com.photoconnect.service;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.InvalidCredentialsException;
import com.photoconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User authenticate(LoginRequest loginRequest) {
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();
        
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Email or password is incorrect.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email or password is incorrect.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountDisabledException("Your account is currently " + user.getStatus().name().toLowerCase() + ".");
        }

        return user;
    }
}
