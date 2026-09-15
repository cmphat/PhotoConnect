package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.PhotographerProfileAlreadyExistsException;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotographerProfileServiceTest {

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PhotographerProfileServiceImpl photographerProfileService;

    private User activeUser;
    private PhotographerProfileRequest validRequest;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("user@example.com");
        activeUser.setFullName("Test User");
        activeUser.setRole(UserRole.CUSTOMER);
        activeUser.setStatus(UserStatus.ACTIVE);

        validRequest = new PhotographerProfileRequest();
        validRequest.setDisplayName("My Photography Studio");
        validRequest.setBio("I am a passionate photographer with experience in portraits and events.");
        validRequest.setCity("Ho Chi Minh City");
        validRequest.setExperienceYears(5);
        validRequest.setPriceFrom(new BigDecimal("1500000"));
    }

    @Test
    void successfulOnboarding_shouldCreateProfileAndPromoteRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(photographerProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(photographerProfileRepository.save(any(PhotographerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PhotographerProfile result = photographerProfileService.createProfile(1L, validRequest);

        assertThat(result.getUser()).isEqualTo(activeUser);
        assertThat(result.getDisplayName()).isEqualTo("My Photography Studio");
        assertThat(result.getCity()).isEqualTo("Ho Chi Minh City");
        assertThat(result.getExperienceYears()).isEqualTo(5);
        assertThat(result.getPriceFrom()).isEqualByComparingTo(new BigDecimal("1500000"));
        assertThat(result.getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.PENDING);
        assertThat(activeUser.getRole()).isEqualTo(UserRole.PHOTOGRAPHER);

        verify(userRepository).save(activeUser);
        verify(photographerProfileRepository).save(any(PhotographerProfile.class));
    }

    @Test
    void duplicateProfile_shouldThrowPhotographerProfileAlreadyExistsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(photographerProfileRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(PhotographerProfileAlreadyExistsException.class,
                () -> photographerProfileService.createProfile(1L, validRequest));

        verify(photographerProfileRepository, never()).save(any());
    }

    @Test
    void missingUser_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> photographerProfileService.createProfile(99L, validRequest));
    }

    @Test
    void inactiveUser_shouldThrowAccountDisabledException() {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        assertThrows(AccountDisabledException.class,
                () -> photographerProfileService.createProfile(1L, validRequest));

        verify(photographerProfileRepository, never()).save(any());
    }

    @Test
    void bannedUser_shouldThrowAccountDisabledException() {
        activeUser.setStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        assertThrows(AccountDisabledException.class,
                () -> photographerProfileService.createProfile(1L, validRequest));

        verify(photographerProfileRepository, never()).save(any());
    }

    @Test
    void adminUser_shouldNotBeDemotedThroughPhotographerOnboarding() {
        activeUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> photographerProfileService.createProfile(1L, validRequest));

        assertThat(error.getMessage()).contains("Only customer accounts");
        verify(photographerProfileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
