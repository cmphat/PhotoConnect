package com.photoconnect.service;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.EmailAlreadyExistsException;
import com.photoconnect.exception.PasswordMismatchException;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    public void cleanup() {
        // Must delete child records first to avoid FK constraint violation
        photographerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testSuccessfulRegistration() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                " JOhN.Doe@example.com ",
                "1234567890",
                "password123",
                "password123"
        );

        // Act
        User createdUser = userService.registerUser(request);

        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getFullName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(createdUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(createdUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        // Verify BCrypt hashing
        assertThat(createdUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", createdUser.getPassword())).isTrue();
        
        // Verify persistence
        User savedUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    public void testDuplicateEmail() {
        // Arrange
        RegisterRequest firstRequest = new RegisterRequest("Alice", "test@example.com", null, "pass1234", "pass1234");
        userService.registerUser(firstRequest);

        RegisterRequest duplicateRequest = new RegisterRequest("Bob", "TEST@example.com", null, "pass5678", "pass5678");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(duplicateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email is already registered");
    }

    @Test
    public void testPasswordMismatch() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Charlie", "charlie@example.com", null, "password123", "password456");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessageContaining("Passwords do not match");

        // Verify not saved
        assertThat(userRepository.existsByEmail("charlie@example.com")).isFalse();
    }
}
