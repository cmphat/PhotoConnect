package com.photoconnect.service;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.exception.InvalidStatusTransitionException;
import com.photoconnect.repository.PhotographerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPhotographerServiceTest {

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @InjectMocks
    private AdminPhotographerServiceImpl adminPhotographerService;

    private User photographerUser;
    private PhotographerProfile pendingProfile;
    private PhotographerProfile approvedProfile;

    @BeforeEach
    void setUp() {
        photographerUser = new User();
        photographerUser.setId(1L);
        photographerUser.setEmail("photographer@example.com");
        photographerUser.setFullName("Test Photographer");
        photographerUser.setRole(UserRole.PHOTOGRAPHER);
        photographerUser.setStatus(UserStatus.ACTIVE);

        pendingProfile = new PhotographerProfile();
        pendingProfile.setUser(photographerUser);
        pendingProfile.setDisplayName("Studio Alpha");
        pendingProfile.setCity("Hanoi");
        pendingProfile.setExperienceYears(3);
        pendingProfile.setPriceFrom(new BigDecimal("800000"));
        pendingProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        approvedProfile = new PhotographerProfile();
        approvedProfile.setUser(photographerUser);
        approvedProfile.setDisplayName("Studio Beta");
        approvedProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
    }

    // ── List tests ──────────────────────────────────────────────────────

    @Test
    void listPending_shouldReturnOnlyPendingProfiles() {
        // Service now calls findByVerificationStatusWithUser (JOIN FETCH)
        when(photographerProfileRepository.findByVerificationStatusWithUser(
                PhotographerVerificationStatus.PENDING))
                .thenReturn(List.of(pendingProfile));

        List<PhotographerProfile> result = adminPhotographerService.listPendingApplications();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.PENDING);
        verify(photographerProfileRepository)
                .findByVerificationStatusWithUser(PhotographerVerificationStatus.PENDING);
    }

    /**
     * Regression test for the lazy-proxy bug:
     * After listPendingApplications() returns, user fields must be readable
     * without an active Hibernate session (simulating JSP access with
     * open-in-view=false). The JOIN FETCH query pre-loads the User, so this
     * must not throw LazyInitializationException.
     */
    @Test
    void listPending_returnedProfiles_mustHaveAccessibleUserEmail() {
        when(photographerProfileRepository.findByVerificationStatusWithUser(
                PhotographerVerificationStatus.PENDING))
                .thenReturn(List.of(pendingProfile));

        List<PhotographerProfile> result = adminPhotographerService.listPendingApplications();

        // Simulates what the JSP does: ${app.user.email}
        assertThat(result.get(0).getUser()).isNotNull();
        assertThat(result.get(0).getUser().getEmail()).isEqualTo("photographer@example.com");
        assertThat(result.get(0).getUser().getFullName()).isEqualTo("Test Photographer");
    }

    // ── Detail tests ────────────────────────────────────────────────────

    /**
     * Regression test for the lazy-proxy bug on the detail page:
     * getApplicationById() must return a profile whose User is pre-loaded.
     */
    @Test
    void getApplicationById_returnedProfile_mustHaveAccessibleUserFields() {
        when(photographerProfileRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(pendingProfile));

        PhotographerProfile result = adminPhotographerService.getApplicationById(1L);

        // Simulates what the JSP does: ${profile.user.email}, ${profile.user.fullName}
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo("photographer@example.com");
        assertThat(result.getUser().getFullName()).isEqualTo("Test Photographer");
    }

    @Test
    void getApplicationById_missingProfile_shouldThrowIllegalArgumentException() {
        when(photographerProfileRepository.findByIdWithUser(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> adminPhotographerService.getApplicationById(99L));
    }

    // ── Approve tests ───────────────────────────────────────────────────

    @Test
    void approve_pendingProfile_shouldTransitionToApproved() {
        when(photographerProfileRepository.findById(1L)).thenReturn(Optional.of(pendingProfile));
        when(photographerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PhotographerProfile result = adminPhotographerService.approve(1L);

        assertThat(result.getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.APPROVED);
        verify(photographerProfileRepository).save(pendingProfile);
    }

    @Test
    void approve_alreadyApprovedProfile_shouldThrowInvalidStatusTransitionException() {
        when(photographerProfileRepository.findById(2L)).thenReturn(Optional.of(approvedProfile));

        assertThrows(InvalidStatusTransitionException.class,
                () -> adminPhotographerService.approve(2L));

        verify(photographerProfileRepository, never()).save(any());
    }

    // ── Reject tests ────────────────────────────────────────────────────

    @Test
    void reject_pendingProfile_shouldTransitionToRejected() {
        when(photographerProfileRepository.findById(1L)).thenReturn(Optional.of(pendingProfile));
        when(photographerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PhotographerProfile result = adminPhotographerService.reject(1L);

        assertThat(result.getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.REJECTED);
        // User.role must remain PHOTOGRAPHER even after rejection
        assertThat(photographerUser.getRole()).isEqualTo(UserRole.PHOTOGRAPHER);
        verify(photographerProfileRepository).save(pendingProfile);
    }

    @Test
    void reject_alreadyApprovedProfile_shouldThrowInvalidStatusTransitionException() {
        when(photographerProfileRepository.findById(2L)).thenReturn(Optional.of(approvedProfile));

        assertThrows(InvalidStatusTransitionException.class,
                () -> adminPhotographerService.reject(2L));

        verify(photographerProfileRepository, never()).save(any());
    }
}
