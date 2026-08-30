package com.photoconnect.repository;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
public class UserRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    private String testEmail;

    @AfterEach
    public void cleanup() {
        if (testEmail != null) {
            userRepository.findByEmail(testEmail).ifPresent(user -> userRepository.delete(user));
        }
    }

    @Test
    public void testUserPersistence() {
        testEmail = "test-" + UUID.randomUUID() + "@photoconnect.local";

        User user = new User(
                testEmail,
                "dummy_password",
                "Test User",
                "123456789",
                UserRole.CUSTOMER,
                UserStatus.ACTIVE
        );

        // 1. Save a User and verify generated ID is not null
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId(), "Generated ID should not be null after saving");

        // 2. findByEmail returns the stored user
        Optional<User> foundUserOpt = userRepository.findByEmail(testEmail);
        assertTrue(foundUserOpt.isPresent(), "User should be found by email");
        User foundUser = foundUserOpt.get();
        assertEquals("Test User", foundUser.getFullName());
        assertEquals(UserRole.CUSTOMER, foundUser.getRole());
        assertEquals(UserStatus.ACTIVE, foundUser.getStatus());
        
        // 3. existsByEmail works
        assertTrue(userRepository.existsByEmail(testEmail), "existsByEmail should return true for existing email");
        assertFalse(userRepository.existsByEmail("non-existent-" + UUID.randomUUID() + "@photoconnect.local"), "existsByEmail should return false for non-existing email");
    }
}
