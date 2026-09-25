package com.photoconnect.service;

import com.photoconnect.dto.PhotographerProfileEditRequest;
import com.photoconnect.dto.PhotographerProfileRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerSpecialty;
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
import java.util.List;
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

    @Test
    void updateProfile_successful_updatesFieldsAndPreservesVerificationStatusAndRole() {
        activeUser.setRole(UserRole.PHOTOGRAPHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(10L);
        profile.setUser(activeUser);
        profile.setDisplayName("Old Name");
        profile.setBio("Old bio text with enough length.");
        profile.setCity("Old City");
        profile.setExperienceYears(2);
        profile.setPriceFrom(new BigDecimal("1000000"));
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        profile.setAverageRating(4.8);
        profile.setReviewCount(15);

        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(photographerProfileRepository.save(any(PhotographerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PhotographerProfileEditRequest edit = new PhotographerProfileEditRequest();
        edit.setDisplayName("Studio Atelier");
        edit.setHeadline("Editorial & Fine Art");
        edit.setBio("Updated artistic bio describing editorial visions.");
        edit.setCity("Da Nang");
        edit.setCountry("Vietnam");
        edit.setExperienceYears(8);
        edit.setPriceFrom(new BigDecimal("3500000"));
        edit.setSpecialties(List.of("PORTRAIT", "FASHION"));
        edit.setWebsiteUrl("https://atelier.art");
        edit.setInstagramUrl("https://instagram.com/atelier");
        edit.setFacebookUrl("https://facebook.com/atelier");
        edit.setEquipmentSummary("Hasselblad X2D, 55mm");
        edit.setLanguages("English, Vietnamese");
        edit.setTravelAvailable(true);

        PhotographerProfile updated = photographerProfileService.updateProfile(1L, edit);

        assertThat(updated.getDisplayName()).isEqualTo("Studio Atelier");
        assertThat(updated.getHeadline()).isEqualTo("Editorial & Fine Art");
        assertThat(updated.getBio()).isEqualTo("Updated artistic bio describing editorial visions.");
        assertThat(updated.getCity()).isEqualTo("Da Nang");
        assertThat(updated.getCountry()).isEqualTo("Vietnam");
        assertThat(updated.getExperienceYears()).isEqualTo(8);
        assertThat(updated.getPriceFrom()).isEqualByComparingTo(new BigDecimal("3500000"));
        assertThat(updated.getSpecialties()).isEqualTo("PORTRAIT,FASHION");
        assertThat(updated.getWebsiteUrl()).isEqualTo("https://atelier.art");
        assertThat(updated.getInstagramUrl()).isEqualTo("https://instagram.com/atelier");
        assertThat(updated.getFacebookUrl()).isEqualTo("https://facebook.com/atelier");
        assertThat(updated.getEquipmentSummary()).isEqualTo("Hasselblad X2D, 55mm");
        assertThat(updated.getLanguages()).isEqualTo("English, Vietnamese");
        assertThat(updated.isTravelAvailable()).isTrue();

        // Preserved critical invariants
        assertThat(updated.getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.APPROVED);
        assertThat(updated.getAverageRating()).isEqualTo(4.8);
        assertThat(updated.getReviewCount()).isEqualTo(15);
        assertThat(activeUser.getRole()).isEqualTo(UserRole.PHOTOGRAPHER);
    }

    @Test
    void updateProfile_customerRole_shouldThrowIllegalStateException() {
        activeUser.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        PhotographerProfileEditRequest edit = new PhotographerProfileEditRequest();
        edit.setDisplayName("Test Name");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> photographerProfileService.updateProfile(1L, edit));
        assertThat(ex.getMessage()).contains("Only photographer accounts");
    }

    @Test
    void updateProfile_unsafeUrl_shouldThrowIllegalArgumentException() {
        activeUser.setRole(UserRole.PHOTOGRAPHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(activeUser);
        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        PhotographerProfileEditRequest edit = new PhotographerProfileEditRequest();
        edit.setDisplayName("Valid Name");
        edit.setBio("Valid bio with enough characters to pass.");
        edit.setCity("Hanoi");
        edit.setWebsiteUrl("javascript:alert('pwned')");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> photographerProfileService.updateProfile(1L, edit));
        assertThat(ex.getMessage()).contains("Website URL must start with http:// or https://");
    }

    @Test
    void updateProfile_invalidSpecialty_shouldThrowIllegalArgumentException() {
        activeUser.setRole(UserRole.PHOTOGRAPHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(activeUser);
        when(photographerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        PhotographerProfileEditRequest edit = new PhotographerProfileEditRequest();
        edit.setDisplayName("Valid Name");
        edit.setBio("Valid bio with enough characters to pass.");
        edit.setCity("Hanoi");
        edit.setSpecialties(List.of("<script>hacked</script>"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> photographerProfileService.updateProfile(1L, edit));
        assertThat(ex.getMessage()).contains("Invalid photography specialty");
    }

    @Test
    void calculateProfileCompleteness_scoresCorrectly() {
        // Minimal profile: name (10%)
        PhotographerProfile minimal = new PhotographerProfile();
        minimal.setDisplayName("Artist");
        int scoreMinimal = photographerProfileService.calculateProfileCompleteness(minimal, 0, false);
        assertThat(scoreMinimal).isEqualTo(10);

        // Add headline (+15%), bio (+15%), location (+10%), specialties (+15%), starting price (+10%)
        minimal.setHeadline("Professional Portrait Artist");
        minimal.setBio("Passionate photographer capturing authentic and timeless moments.");
        minimal.setCity("Saigon");
        minimal.setSpecialties("PORTRAIT");
        minimal.setPriceFrom(new BigDecimal("2000000"));
        int scoreWithoutMedia = photographerProfileService.calculateProfileCompleteness(minimal, 0, false);
        assertThat(scoreWithoutMedia).isEqualTo(75);

        // Add 1 portfolio photo (+15%) and designated cover (+10%) -> 100%
        int scoreComplete = photographerProfileService.calculateProfileCompleteness(minimal, 2, true);
        assertThat(scoreComplete).isEqualTo(100);
    }
}
