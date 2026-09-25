package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.SavedPhotographer;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Live SQL Server integration tests for SavedPhotographerRepository.
 * Only executed when DB_USERNAME environment variable is present.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
class SavedPhotographerRepositoryIntegrationTest {

    @Autowired
    private SavedPhotographerRepository savedPhotographerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    private User testCustomer;
    private User testPhotographerUser;
    private PhotographerProfile testProfile;

    @BeforeEach
    void setUp() {
        testCustomer = new User();
        testCustomer.setEmail("saved.customer.test@example.com");
        testCustomer.setPassword("hashedpassword123");
        testCustomer.setFullName("Saved Test Customer");
        testCustomer.setPhone("0901112233");
        testCustomer.setRole(UserRole.CUSTOMER);
        testCustomer.setStatus(UserStatus.ACTIVE);
        userRepository.save(testCustomer);

        testPhotographerUser = new User();
        testPhotographerUser.setEmail("saved.photographer.test@example.com");
        testPhotographerUser.setPassword("hashedpassword123");
        testPhotographerUser.setFullName("Saved Test Artist");
        testPhotographerUser.setPhone("0904445566");
        testPhotographerUser.setRole(UserRole.PHOTOGRAPHER);
        testPhotographerUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(testPhotographerUser);

        testProfile = new PhotographerProfile();
        testProfile.setUser(testPhotographerUser);
        testProfile.setDisplayName("Saved Artist Studio");
        testProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        testProfile.setPriceFrom(new BigDecimal("2000000.00"));
        photographerProfileRepository.save(testProfile);
    }

    @AfterEach
    void tearDown() {
        if (testCustomer != null && testProfile != null) {
            savedPhotographerRepository.deleteByCustomerIdAndPhotographerProfileId(testCustomer.getId(), testProfile.getId());
        }
        if (testProfile != null && testProfile.getId() != null) {
            photographerProfileRepository.delete(testProfile);
        }
        if (testPhotographerUser != null && testPhotographerUser.getId() != null) {
            userRepository.delete(testPhotographerUser);
        }
        if (testCustomer != null && testCustomer.getId() != null) {
            userRepository.delete(testCustomer);
        }
    }

    @Test
    @DisplayName("Persist and lookup saved photographer by customer and profile")
    void persistAndLookupSavedPhotographer() {
        SavedPhotographer saved = new SavedPhotographer(testCustomer, testProfile);
        savedPhotographerRepository.save(saved);

        boolean exists = savedPhotographerRepository
                .existsByCustomerIdAndPhotographerProfileId(testCustomer.getId(), testProfile.getId());
        assertThat(exists).isTrue();

        Optional<SavedPhotographer> found = savedPhotographerRepository
                .findByCustomerIdAndPhotographerProfileId(testCustomer.getId(), testProfile.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomer().getId()).isEqualTo(testCustomer.getId());
        assertThat(found.get().getPhotographerProfile().getId()).isEqualTo(testProfile.getId());
    }

    @Test
    @DisplayName("Unique constraint rejects duplicate save for same customer and profile")
    void uniqueConstraintPreventsDuplicates() {
        SavedPhotographer first = new SavedPhotographer(testCustomer, testProfile);
        savedPhotographerRepository.saveAndFlush(first);

        SavedPhotographer duplicate = new SavedPhotographer(testCustomer, testProfile);
        assertThatThrownBy(() -> savedPhotographerRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Delete by customer and photographer removes record")
    void deleteByCustomerAndPhotographer() {
        SavedPhotographer saved = new SavedPhotographer(testCustomer, testProfile);
        savedPhotographerRepository.save(saved);

        savedPhotographerRepository.deleteByCustomerIdAndPhotographerProfileId(testCustomer.getId(), testProfile.getId());

        boolean exists = savedPhotographerRepository
                .existsByCustomerIdAndPhotographerProfileId(testCustomer.getId(), testProfile.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Query approved saved photographers returns profile with user eagerly")
    void queryApprovedSavedPhotographersWithProfile() {
        SavedPhotographer saved = new SavedPhotographer(testCustomer, testProfile);
        savedPhotographerRepository.save(saved);

        List<SavedPhotographer> list = savedPhotographerRepository
                .findApprovedByCustomerIdWithProfile(testCustomer.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getPhotographerProfile().getDisplayName()).isEqualTo("Saved Artist Studio");
    }
}
