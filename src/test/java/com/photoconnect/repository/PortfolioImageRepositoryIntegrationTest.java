package com.photoconnect.repository;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PortfolioImageRepository against the real SQL Server database.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
class PortfolioImageRepositoryIntegrationTest {

    @Autowired
    private PortfolioImageRepository portfolioImageRepository;

    @Autowired
    private PhotographerProfileRepository photographerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private PhotographerProfile testProfile;

    @BeforeEach
    void setUp() {
        // Create a test user + photographer profile
        testUser = new User();
        testUser.setEmail("portfolio.test.repo@example.com");
        testUser.setPassword("hashedpassword123");
        testUser.setFullName("Portfolio Test User");
        testUser.setPhone("0901234567");
        testUser.setRole(UserRole.PHOTOGRAPHER);
        testUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(testUser);

        testProfile = new PhotographerProfile();
        testProfile.setUser(testUser);
        testProfile.setDisplayName("Portfolio Test Studio");
        testProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        testProfile.setPriceFrom(new BigDecimal("3000000.00"));
        photographerProfileRepository.save(testProfile);
    }

    @AfterEach
    void tearDown() {
        // Clean up: portfolio images → profile → user (FK order)
        portfolioImageRepository.findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(testProfile.getId())
                .forEach(portfolioImageRepository::delete);
        photographerProfileRepository.delete(testProfile);
        userRepository.delete(testUser);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    @Test
    void shouldSaveAndRetrievePortfolioImage() {
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(testProfile);
        image.setImageUrl("https://res.cloudinary.com/test/image/upload/v1/photoconnect/portfolio/test001.jpg");
        image.setPublicId("photoconnect/portfolio/test001");
        image.setCaption("My first portfolio image");
        image.setDisplayOrder(1);

        PortfolioImage saved = portfolioImageRepository.save(image);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getImageUrl()).contains("test001");
        assertThat(saved.getPublicId()).isEqualTo("photoconnect/portfolio/test001");
        assertThat(saved.getCaption()).isEqualTo("My first portfolio image");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getPhotographerProfile().getId()).isEqualTo(testProfile.getId());
    }

    @Test
    void shouldSaveImageWithNullCaption() {
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(testProfile);
        image.setImageUrl("https://res.cloudinary.com/test/image/upload/v1/abc.jpg");
        image.setPublicId("photoconnect/portfolio/abc");

        PortfolioImage saved = portfolioImageRepository.save(image);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCaption()).isNull();
    }

    // ── findByPhotographerProfileId ordering ──────────────────────────────────

    /**
     * Verifies that findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc
     * returns images in correct order: displayOrder ASC first, then newest-first.
     */
    @Test
    void findByPhotographerProfileId_shouldReturnImagesOrderedByDisplayOrderThenNewest() {
        // Two images with the same displayOrder — order within tier is by createdAt DESC
        PortfolioImage img1 = new PortfolioImage();
        img1.setPhotographerProfile(testProfile);
        img1.setImageUrl("https://res.cloudinary.com/test/img1.jpg");
        img1.setPublicId("photoconnect/portfolio/img1");
        img1.setDisplayOrder(0);
        portfolioImageRepository.save(img1);

        PortfolioImage img2 = new PortfolioImage();
        img2.setPhotographerProfile(testProfile);
        img2.setImageUrl("https://res.cloudinary.com/test/img2.jpg");
        img2.setPublicId("photoconnect/portfolio/img2");
        img2.setDisplayOrder(0);
        portfolioImageRepository.save(img2);

        PortfolioImage imgHighOrder = new PortfolioImage();
        imgHighOrder.setPhotographerProfile(testProfile);
        imgHighOrder.setImageUrl("https://res.cloudinary.com/test/imgH.jpg");
        imgHighOrder.setPublicId("photoconnect/portfolio/imgH");
        imgHighOrder.setDisplayOrder(10);
        portfolioImageRepository.save(imgHighOrder);

        List<PortfolioImage> results = portfolioImageRepository
                .findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(testProfile.getId());

        assertThat(results).hasSize(3);
        // imgHighOrder (displayOrder=10) should be LAST
        assertThat(results.get(2).getPublicId()).isEqualTo("photoconnect/portfolio/imgH");
        // First two (displayOrder=0) should be present in the first two positions
        assertThat(results.subList(0, 2))
                .extracting(PortfolioImage::getDisplayOrder)
                .containsOnly(0);
    }

    // ── findByIdAndPhotographerProfileId (ownership) ──────────────────────────

    /**
     * Ownership-safe lookup: returns image when it belongs to the specified profile.
     */
    @Test
    void findByIdAndPhotographerProfileId_ownImage_shouldReturnResult() {
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(testProfile);
        image.setImageUrl("https://res.cloudinary.com/test/own.jpg");
        image.setPublicId("photoconnect/portfolio/own");
        PortfolioImage saved = portfolioImageRepository.save(image);

        Optional<PortfolioImage> result = portfolioImageRepository
                .findByIdAndPhotographerProfileId(saved.getId(), testProfile.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getPublicId()).isEqualTo("photoconnect/portfolio/own");
    }

    /**
     * Ownership protection: if the imageId belongs to a different profile,
     * the query must return EMPTY — not the image.
     * This prevents cross-profile delete attacks.
     */
    @Test
    void findByIdAndPhotographerProfileId_wrongProfileId_shouldReturnEmpty() {
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(testProfile);
        image.setImageUrl("https://res.cloudinary.com/test/own2.jpg");
        image.setPublicId("photoconnect/portfolio/own2");
        PortfolioImage saved = portfolioImageRepository.save(image);

        // Use profileId = 999999 (does not exist) — ownership mismatch
        Optional<PortfolioImage> result = portfolioImageRepository
                .findByIdAndPhotographerProfileId(saved.getId(), 999999L);

        assertThat(result).isEmpty();
    }

    // ── countByPhotographerProfileId ──────────────────────────────────────────

    @Test
    void countByPhotographerProfileId_shouldReturnCorrectCount() {
        assertThat(portfolioImageRepository.countByPhotographerProfileId(testProfile.getId())).isEqualTo(0);

        PortfolioImage img1 = new PortfolioImage();
        img1.setPhotographerProfile(testProfile);
        img1.setImageUrl("https://res.cloudinary.com/test/c1.jpg");
        img1.setPublicId("photoconnect/portfolio/c1");
        portfolioImageRepository.save(img1);

        PortfolioImage img2 = new PortfolioImage();
        img2.setPhotographerProfile(testProfile);
        img2.setImageUrl("https://res.cloudinary.com/test/c2.jpg");
        img2.setPublicId("photoconnect/portfolio/c2");
        portfolioImageRepository.save(img2);

        assertThat(portfolioImageRepository.countByPhotographerProfileId(testProfile.getId())).isEqualTo(2);
    }

    // ── Category and Cover Persistence (TASK-B02) ─────────────────────────────

    @Test
    void shouldSaveAndRetrieveCoverAndCategory() {
        PortfolioImage image = new PortfolioImage();
        image.setPhotographerProfile(testProfile);
        image.setImageUrl("https://res.cloudinary.com/test/image/upload/v1/wedding.jpg");
        image.setPublicId("photoconnect/portfolio/wedding");
        image.setCaption("Bridal portrait");
        image.setCategory(PortfolioCategory.WEDDING);
        image.setCover(true);

        PortfolioImage saved = portfolioImageRepository.save(image);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategory()).isEqualTo(PortfolioCategory.WEDDING);
        assertThat(saved.isCover()).isTrue();
    }

    @Test
    void findByPhotographerProfileIdAndIsCoverTrue_shouldReturnOnlyCover() {
        PortfolioImage img1 = new PortfolioImage();
        img1.setPhotographerProfile(testProfile);
        img1.setImageUrl("https://res.cloudinary.com/test/normal.jpg");
        img1.setPublicId("photoconnect/portfolio/normal");
        img1.setCover(false);
        portfolioImageRepository.save(img1);

        PortfolioImage img2 = new PortfolioImage();
        img2.setPhotographerProfile(testProfile);
        img2.setImageUrl("https://res.cloudinary.com/test/cover.jpg");
        img2.setPublicId("photoconnect/portfolio/cover");
        img2.setCover(true);
        portfolioImageRepository.save(img2);

        List<PortfolioImage> covers = portfolioImageRepository
                .findByPhotographerProfileIdAndIsCoverTrue(testProfile.getId());

        assertThat(covers).hasSize(1);
        assertThat(covers.get(0).getPublicId()).isEqualTo("photoconnect/portfolio/cover");
    }
}
