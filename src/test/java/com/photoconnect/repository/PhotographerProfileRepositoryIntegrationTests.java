package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
public class PhotographerProfileRepositoryIntegrationTests {

    @Autowired
    private PhotographerProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("photographer.test@example.com");
        testUser.setPassword("hashedpassword123");
        testUser.setFullName("Test Photographer");
        testUser.setPhone("0987654321");
        testUser.setRole(UserRole.PHOTOGRAPHER);
        testUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up child first to avoid constraint violations
        profileRepository.findByUserId(testUser.getId()).ifPresent(profileRepository::delete);
        userRepository.delete(testUser);
    }

    @Test
    void shouldSaveAndRetrievePhotographerProfile() {
        // Arrange
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(testUser);
        profile.setDisplayName("Test Photography Studio");
        profile.setBio("I am a professional test photographer.");
        profile.setCity("Ho Chi Minh City");
        profile.setExperienceYears(5);
        profile.setPriceFrom(new BigDecimal("1500000.00"));
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        // Act
        PhotographerProfile savedProfile = profileRepository.save(profile);

        // Assert
        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedProfile.getVerificationStatus()).isEqualTo(PhotographerVerificationStatus.PENDING);
        assertThat(savedProfile.getPriceFrom()).isEqualByComparingTo(new BigDecimal("1500000"));

        Optional<PhotographerProfile> retrievedProfile = profileRepository.findByUserId(testUser.getId());
        assertThat(retrievedProfile).isPresent();
        assertThat(retrievedProfile.get().getDisplayName()).isEqualTo("Test Photography Studio");

        boolean exists = profileRepository.existsByUserId(testUser.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldEnforceUniqueUserConstraint() {
        // Arrange
        PhotographerProfile profile1 = new PhotographerProfile();
        profile1.setUser(testUser);
        profile1.setDisplayName("First Studio");
        profile1.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        profileRepository.save(profile1);

        PhotographerProfile profile2 = new PhotographerProfile();
        profile2.setUser(testUser);
        profile2.setDisplayName("Second Studio");
        profile2.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            profileRepository.saveAndFlush(profile2);
        });
    }

    // ── Public marketplace query integration tests ──────────────────────────

    /**
     * Verifies that findByIdAndVerificationStatusWithUser returns the profile
     * when the stored status matches the queried status (APPROVED).
     * This is the query used by the public detail page.
     */
    @Test
    void findByIdAndVerificationStatusWithUser_approvedProfile_shouldReturnResult() {
        // Arrange
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(testUser);
        profile.setDisplayName("Public Studio");
        profile.setBio("Public bio text.");
        profile.setCity("Da Nang");
        profile.setExperienceYears(3);
        profile.setPriceFrom(new BigDecimal("2000000.00"));
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        PhotographerProfile saved = profileRepository.save(profile);

        // Act
        Optional<PhotographerProfile> result = profileRepository
                .findByIdAndVerificationStatusWithUser(saved.getId(), PhotographerVerificationStatus.APPROVED);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDisplayName()).isEqualTo("Public Studio");
        // User must be initialized (JOIN FETCH) — no LazyInitializationException
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getEmail()).isEqualTo("photographer.test@example.com");
    }

    /**
     * Verifies that findByIdAndVerificationStatusWithUser returns EMPTY
     * when the stored status is PENDING but APPROVED is queried.
     * This is the server-side gatekeeper for the public marketplace:
     * a PENDING photographer cannot be accessed by guessing their ID.
     */
    @Test
    void findByIdAndVerificationStatusWithUser_pendingProfile_queriedAsApproved_shouldReturnEmpty() {
        // Arrange
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(testUser);
        profile.setDisplayName("Pending Studio");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        PhotographerProfile saved = profileRepository.save(profile);

        // Act
        Optional<PhotographerProfile> result = profileRepository
                .findByIdAndVerificationStatusWithUser(saved.getId(), PhotographerVerificationStatus.APPROVED);

        // Assert — PENDING profile must NOT be returned when queried as APPROVED
        assertThat(result).isEmpty();
    }

    // ── Search & filter query integration tests (TASK-012) ──────────────────

    @Test
    void searchApprovedPhotographers_shouldFilterByKeywordCityPriceAndExperience() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(testUser);
        profile.setDisplayName("Cinematic Portrait Studio");
        profile.setBio("High-end fashion and wedding photography.");
        profile.setCity("Ho Chi Minh City");
        profile.setExperienceYears(6);
        profile.setPriceFrom(new BigDecimal("4500000.00"));
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        profileRepository.save(profile);

        // 1. Keyword match in bio
        var keywordResults = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, "fashion", null, null, null, null);
        assertThat(keywordResults).isNotEmpty();

        // 2. City match
        var cityResults = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, null, "Ho Chi Minh", null, null, null);
        assertThat(cityResults).isNotEmpty();

        // 3. Price range match
        var priceResults = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, null, null,
                new BigDecimal("4000000"), new BigDecimal("5000000"), null);
        assertThat(priceResults).isNotEmpty();

        // 4. Experience match
        var expResults = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, null, null, null, null, 5);
        assertThat(expResults).isNotEmpty();

        // 5. Excluded by experience threshold
        var expExcluded = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, null, null, null, null, 10);
        assertThat(expExcluded).isEmpty();
    }

    @Test
    void searchApprovedPhotographers_pendingProfile_shouldNotBeReturned() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(testUser);
        profile.setDisplayName("Pending Secret Studio");
        profile.setCity("Hanoi");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        profileRepository.save(profile);

        var results = profileRepository.searchApprovedPhotographers(
                PhotographerVerificationStatus.APPROVED, "Secret", null, null, null, null);
        assertThat(results).isEmpty();
    }
}

