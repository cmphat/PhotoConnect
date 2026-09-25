package com.photoconnect.service;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.SavedPhotographerDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.SavedPhotographer;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.SavedPhotographerRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedPhotographerServiceTest {

    @Mock
    private SavedPhotographerRepository savedPhotographerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private PublicPhotographerService publicPhotographerService;

    @InjectMocks
    private SavedPhotographerServiceImpl savedPhotographerService;

    private User customerUser;
    private User photographerUser;
    private User adminUser;
    private PhotographerProfile approvedProfile;
    private PhotographerProfile pendingProfile;

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setId(100L);
        customerUser.setEmail("customer@example.com");
        customerUser.setFullName("Nguyen Van Customer");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setStatus(UserStatus.ACTIVE);

        photographerUser = new User();
        photographerUser.setId(200L);
        photographerUser.setEmail("photographer@example.com");
        photographerUser.setFullName("Le Van Photographer");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        adminUser = new User();
        adminUser.setId(300L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);

        approvedProfile = new PhotographerProfile();
        approvedProfile.setId(10L);
        approvedProfile.setUser(photographerUser);
        approvedProfile.setDisplayName("Le Photographer Studio");
        approvedProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        approvedProfile.setPriceFrom(new BigDecimal("2500000.00"));

        pendingProfile = new PhotographerProfile();
        pendingProfile.setId(20L);
        pendingProfile.setUser(photographerUser);
        pendingProfile.setDisplayName("Pending Studio");
        pendingProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
    }

    @Test
    @DisplayName("Customer can save an approved photographer")
    void customerCanSaveApprovedPhotographer() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(customerUser));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(approvedProfile));
        when(savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(100L, 10L)).thenReturn(false);

        boolean result = savedPhotographerService.savePhotographer(100L, 10L);

        assertThat(result).isTrue();
        verify(savedPhotographerRepository).save(any(SavedPhotographer.class));
    }

    @Test
    @DisplayName("Saving already saved photographer is idempotent and does not create duplicates")
    void duplicateSaveIsIdempotent() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(customerUser));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(approvedProfile));
        when(savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(100L, 10L)).thenReturn(true);

        boolean result = savedPhotographerService.savePhotographer(100L, 10L);

        assertThat(result).isTrue();
        verify(savedPhotographerRepository, never()).save(any(SavedPhotographer.class));
    }

    @Test
    @DisplayName("Concurrent duplicate save caught by DB unique constraint is handled gracefully")
    void concurrentDuplicateSaveCaughtByUniqueConstraintIsGraceful() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(customerUser));
        when(photographerProfileRepository.findById(10L)).thenReturn(Optional.of(approvedProfile));
        when(savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(100L, 10L)).thenReturn(false);
        when(savedPhotographerRepository.save(any())).thenThrow(new DataIntegrityViolationException("UQ_saved_photographers violation"));

        boolean result = savedPhotographerService.savePhotographer(100L, 10L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Unapproved photographer cannot be saved")
    void unapprovedPhotographerCannotBeSaved() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(customerUser));
        when(photographerProfileRepository.findById(20L)).thenReturn(Optional.of(pendingProfile));

        assertThatThrownBy(() -> savedPhotographerService.savePhotographer(100L, 20L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only approved photographers");

        verify(savedPhotographerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Photographer or Admin user cannot use customer favorite mutation")
    void nonCustomerCannotSavePhotographer() {
        when(userRepository.findById(200L)).thenReturn(Optional.of(photographerUser));
        when(userRepository.findById(300L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> savedPhotographerService.savePhotographer(200L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only users with CUSTOMER role");

        assertThatThrownBy(() -> savedPhotographerService.savePhotographer(300L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only users with CUSTOMER role");

        verify(savedPhotographerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Null customer id (guest) throws IllegalArgumentException")
    void nullCustomerIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> savedPhotographerService.savePhotographer(null, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer user ID is required");
    }

    @Test
    @DisplayName("Customer can unsave own photographer")
    void customerCanUnsaveOwnPhotographer() {
        SavedPhotographer saved = new SavedPhotographer(customerUser, approvedProfile);
        when(savedPhotographerRepository.findByCustomerIdAndPhotographerProfileId(100L, 10L))
                .thenReturn(Optional.of(saved));

        boolean result = savedPhotographerService.removeSavedPhotographer(100L, 10L);

        assertThat(result).isTrue();
        verify(savedPhotographerRepository).delete(saved);
    }

    @Test
    @DisplayName("Unsaving an absent favorite returns false gracefully")
    void unsavingAbsentFavoriteReturnsFalseGracefully() {
        when(savedPhotographerRepository.findByCustomerIdAndPhotographerProfileId(100L, 10L))
                .thenReturn(Optional.empty());

        boolean result = savedPhotographerService.removeSavedPhotographer(100L, 10L);

        assertThat(result).isFalse();
        verify(savedPhotographerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Customer A cannot remove Customer B favorite because removal scopes strictly to authenticated user")
    void idorPreventionCustomerACannotRemoveCustomerBFavorite() {
        // Customer 100 attempts to remove profile 10, but profile 10 is only saved by customer 999
        when(savedPhotographerRepository.findByCustomerIdAndPhotographerProfileId(100L, 10L))
                .thenReturn(Optional.empty());

        boolean result = savedPhotographerService.removeSavedPhotographer(100L, 10L);

        assertThat(result).isFalse();
        verify(savedPhotographerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("isSaved returns correct boolean state")
    void isSavedReturnsCorrectState() {
        when(savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(100L, 10L)).thenReturn(true);
        when(savedPhotographerRepository.existsByCustomerIdAndPhotographerProfileId(100L, 99L)).thenReturn(false);

        assertThat(savedPhotographerService.isSaved(100L, 10L)).isTrue();
        assertThat(savedPhotographerService.isSaved(100L, 99L)).isFalse();
        assertThat(savedPhotographerService.isSaved(null, 10L)).isFalse();
        assertThat(savedPhotographerService.isSaved(100L, null)).isFalse();
    }

    @Test
    @DisplayName("getSavedPhotographers returns only approved photographers for customer")
    void getSavedPhotographersReturnsOnlyApprovedPhotographers() {
        SavedPhotographer saved = new SavedPhotographer(customerUser, approvedProfile);
        saved.setId(1L);
        saved.setCreatedAt(LocalDateTime.now());

        when(savedPhotographerRepository.findApprovedByCustomerIdWithProfile(100L))
                .thenReturn(List.of(saved));

        PhotographerPublicDto publicDto = PhotographerPublicDto.from(approvedProfile, null, 80);
        when(publicPhotographerService.getApprovedPhotographerById(10L)).thenReturn(publicDto);

        List<SavedPhotographerDto> list = savedPhotographerService.getSavedPhotographers(100L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPhotographer().getDisplayName()).isEqualTo("Le Photographer Studio");
        assertThat(list.get(0).getPhotographerId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getSavedPhotographerProfileIds returns set of IDs")
    void getSavedPhotographerProfileIdsReturnsSet() {
        when(savedPhotographerRepository.findSavedPhotographerProfileIdsByCustomerId(100L))
                .thenReturn(List.of(10L, 15L));

        Set<Long> set = savedPhotographerService.getSavedPhotographerProfileIds(100L);

        assertThat(set).containsExactlyInAnyOrder(10L, 15L);
        assertThat(savedPhotographerService.getSavedPhotographerProfileIds(null)).isEmpty();
    }

    @Test
    @DisplayName("countSavedPhotographers returns accurate count of approved saved creators")
    void countSavedPhotographersReturnsCount() {
        SavedPhotographer saved = new SavedPhotographer(customerUser, approvedProfile);
        when(savedPhotographerRepository.findApprovedByCustomerIdWithProfile(100L))
                .thenReturn(List.of(saved));

        assertThat(savedPhotographerService.countSavedPhotographers(100L)).isEqualTo(1);
        assertThat(savedPhotographerService.countSavedPhotographers(null)).isZero();
    }
}
