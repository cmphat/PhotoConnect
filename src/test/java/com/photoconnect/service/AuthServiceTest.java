package com.photoconnect.service;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.InvalidCredentialsException;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSuccessfulLogin() {
        LoginRequest req = new LoginRequest();
        req.setEmail("TEST@example.com ");
        req.setPassword("Password123");

        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setStatus(UserStatus.ACTIVE);
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        User authenticatedUser = authService.authenticate(req);

        assertNotNull(authenticatedUser);
        assertEquals("test@example.com", authenticatedUser.getEmail());
    }

    @Test
    public void testWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("WrongPassword");

        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("hashedPassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate(req);
        });
        
        assertEquals("Email or password is incorrect.", ex.getMessage());
    }

    @Test
    public void testUnknownEmail() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@example.com");
        req.setPassword("Password123");
        
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate(req);
        });

        assertEquals("Email or password is incorrect.", ex.getMessage());
    }

    @Test
    public void testInactiveAccount() {
        LoginRequest req = new LoginRequest();
        req.setEmail("inactive@example.com");
        req.setPassword("Password123");

        User existingUser = new User();
        existingUser.setEmail("inactive@example.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setStatus(UserStatus.INACTIVE);
        
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        AccountDisabledException ex = assertThrows(AccountDisabledException.class, () -> {
            authService.authenticate(req);
        });

        assertEquals("Your account is currently inactive.", ex.getMessage());
    }

    @Test
    public void testBannedAccount() {
        LoginRequest req = new LoginRequest();
        req.setEmail("banned@example.com");
        req.setPassword("Password123");

        User existingUser = new User();
        existingUser.setEmail("banned@example.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setStatus(UserStatus.BANNED);
        
        when(userRepository.findByEmail("banned@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        AccountDisabledException ex = assertThrows(AccountDisabledException.class, () -> {
            authService.authenticate(req);
        });

        assertEquals("Your account is currently banned.", ex.getMessage());
    }
}
