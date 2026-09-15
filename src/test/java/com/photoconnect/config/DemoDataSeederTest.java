package com.photoconnect.config;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock UserRepository userRepository;
    @Mock PhotographerProfileRepository photographerProfileRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void firstRunCreatesTwoRoleAccountsAndFifteenApprovedPhotographers() {
        AtomicLong ids = new AtomicLong(1);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("PhotoConnectDemo!2026")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(ids.getAndIncrement());
            return user;
        });

        new DemoDataSeeder(userRepository, photographerProfileRepository, passwordEncoder,
                "PhotoConnectDemo!2026").run();

        verify(userRepository, times(17)).save(any(User.class));
        verify(photographerProfileRepository, times(15)).save(any(PhotographerProfile.class));
    }

    @Test
    void existingDemoAccountsAreNeverOverwritten() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode("PhotoConnectDemo!2026")).thenReturn("bcrypt-hash");

        new DemoDataSeeder(userRepository, photographerProfileRepository, passwordEncoder,
                "PhotoConnectDemo!2026").run();

        verify(userRepository, never()).save(any());
        verify(photographerProfileRepository, never()).save(any());
    }

    @Test
    void unsafeDemoPasswordStopsBeforeDatabaseWrites() {
        assertThatThrownBy(() -> new DemoDataSeeder(
                userRepository, photographerProfileRepository, passwordEncoder, "short").run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("12 to 72");

        verify(userRepository, never()).save(any());
        verify(photographerProfileRepository, never()).save(any());
    }
}
