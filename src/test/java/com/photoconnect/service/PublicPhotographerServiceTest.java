package com.photoconnect.service;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.PhotographerSearchRequest;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PublicPhotographerServiceImpl.
 *
 * These tests verify PUBLIC MARKETPLACE behavior, not admin behavior.
 * Key invariant under test: only APPROVED profiles are ever returned.
 */
@ExtendWith(MockitoExtension.class)
class PublicPhotographerServiceTest {

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private PortfolioImageRepository portfolioImageRepository;

    @InjectMocks
    private PublicPhotographerServiceImpl publicPhotographerService;

    private User user;
    private PhotographerProfile approvedProfile;
    private PhotographerProfile pendingProfile;
    private PhotographerProfile rejectedProfile;
    private PhotographerProfile suspendedProfile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("photographer@example.com");
        user.setPassword("$2a$10$hashedpassword");
        user.setFullName("Nguyen Van A");
        user.setRole(UserRole.PHOTOGRAPHER);
        user.setStatus(UserStatus.ACTIVE);

        approvedProfile = new PhotographerProfile();
        approvedProfile.setUser(user);
        approvedProfile.setDisplayName("Studio Sáng Tạo");
        approvedProfile.setBio("Professional wedding and portrait photographer based in Hanoi.");
        approvedProfile.setCity("Hanoi");
        approvedProfile.setExperienceYears(5);
        approvedProfile.setPriceFrom(new BigDecimal("3000000"));
        approvedProfile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);

        try {
            var idField = PhotographerProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(approvedProfile, 10L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        pendingProfile = new PhotographerProfile();
        pendingProfile.setUser(user);
        pendingProfile.setDisplayName("Pending Studio");
        pendingProfile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        rejectedProfile = new PhotographerProfile();
        rejectedProfile.setUser(user);
        rejectedProfile.setDisplayName("Rejected Studio");
        rejectedProfile.setVerificationStatus(PhotographerVerificationStatus.REJECTED);

        suspendedProfile = new PhotographerProfile();
        suspendedProfile.setUser(user);
        suspendedProfile.setDisplayName("Suspended Studio");
        suspendedProfile.setVerificationStatus(PhotographerVerificationStatus.SUSPENDED);
    }

    // ── listApprovedPhotographers() ─────────────────────────────────────────

    /**
     * Core business rule: list returns only APPROVED profiles.
     * The repository is the gatekeeper — it returns only what we ask for.
     */
    @Test
    void listApproved_shouldReturnOnlyApprovedProfiles() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(approvedProfile));

        List<PhotographerPublicDto> result = publicPhotographerService.listApprovedPhotographers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Studio Sáng Tạo");
        verify(photographerProfileRepository)
                .searchApprovedPhotographers(
                        eq(PhotographerVerificationStatus.APPROVED),
                        isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void listApproved_whenNoneApproved_shouldReturnEmptyList() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        List<PhotographerPublicDto> result = publicPhotographerService.listApprovedPhotographers();

        assertThat(result).isEmpty();
    }

    // ── searchPhotographers() ───────────────────────────────────────────────

    @Test
    void searchPhotographers_noFilters_shouldReturnAllApproved() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Studio Sáng Tạo");
    }

    @Test
    void searchPhotographers_keywordFilter_shouldPassNormalizedKeyword() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                eq("wedding"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword("  wedding  ");

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        verify(photographerProfileRepository).searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                eq("wedding"), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void searchPhotographers_cityFilter_shouldPassNormalizedCity() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), eq("Hanoi"), isNull(), isNull(), isNull()))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setCity("  Hanoi  ");

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        verify(photographerProfileRepository).searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), eq("Hanoi"), isNull(), isNull(), isNull());
    }

    @Test
    void searchPhotographers_priceFilter_shouldPassPriceRange() {
        BigDecimal min = new BigDecimal("1000000");
        BigDecimal max = new BigDecimal("5000000");

        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), eq(min), eq(max), isNull()))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinPrice(min);
        request.setMaxPrice(max);

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        verify(photographerProfileRepository).searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), eq(min), eq(max), isNull());
    }

    @Test
    void searchPhotographers_experienceFilter_shouldPassMinExperience() {
        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), eq(3)))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinExperience(3);

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        verify(photographerProfileRepository).searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), eq(3));
    }

    @Test
    void searchPhotographers_combinedFilters_shouldPassAllParameters() {
        BigDecimal min = new BigDecimal("2000000");
        BigDecimal max = new BigDecimal("6000000");

        when(photographerProfileRepository.searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                eq("portrait"), eq("Da Nang"), eq(min), eq(max), eq(4)))
                .thenReturn(List.of(approvedProfile));

        PhotographerSearchRequest request = new PhotographerSearchRequest(
                "portrait", "Da Nang", min, max, 4
        );

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request);

        assertThat(result).hasSize(1);
        verify(photographerProfileRepository).searchApprovedPhotographers(
                eq(PhotographerVerificationStatus.APPROVED),
                eq("portrait"), eq("Da Nang"), eq(min), eq(max), eq(4));
    }

    @Test
    void searchPhotographers_invalidPriceRange_shouldThrowIllegalArgumentException() {
        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setMinPrice(new BigDecimal("10000000"));
        request.setMaxPrice(new BigDecimal("5000000"));

        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.searchPhotographers(request));

        verify(photographerProfileRepository, never()).searchApprovedPhotographers(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchPhotographers_withPortfolioImage_shouldPopulateCoverImageUrl() {
        PortfolioImage image = new PortfolioImage();
        image.setImageUrl("https://res.cloudinary.com/test/image1.jpg");

        when(photographerProfileRepository.searchApprovedPhotographers(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(approvedProfile));
        when(portfolioImageRepository.findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(10L))
                .thenReturn(List.of(image));

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoverImageUrl()).isEqualTo("https://res.cloudinary.com/test/image1.jpg");
    }

    @Test
    void searchPhotographers_withExplicitCoverImage_shouldPrioritizeExplicitCoverOverFirstImage() {
        PortfolioImage img1 = new PortfolioImage();
        img1.setImageUrl("https://res.cloudinary.com/test/image1.jpg");
        img1.setCover(false);

        PortfolioImage img2Cover = new PortfolioImage();
        img2Cover.setImageUrl("https://res.cloudinary.com/test/cover.jpg");
        img2Cover.setCover(true);

        when(photographerProfileRepository.searchApprovedPhotographers(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(approvedProfile));
        when(portfolioImageRepository.findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(10L))
                .thenReturn(List.of(img1, img2Cover));

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoverImageUrl()).isEqualTo("https://res.cloudinary.com/test/cover.jpg");
    }

    @Test
    void searchPhotographers_withoutPortfolioImage_shouldHaveNullCoverImageUrl() {
        when(photographerProfileRepository.searchApprovedPhotographers(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(approvedProfile));
        when(portfolioImageRepository.findByPhotographerProfileIdOrderByDisplayOrderAscCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoverImageUrl()).isNull();
    }

    @Test
    void searchPhotographers_paged_shouldPreserveTotalsAndApprovedFilter() {
        PageRequest pageable = PageRequest.of(1, 12);
        when(photographerProfileRepository.searchApprovedPhotographersPaged(
                eq(PhotographerVerificationStatus.APPROVED), eq("portrait"), isNull(),
                isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(approvedProfile), pageable, 25));

        PhotographerSearchRequest request = new PhotographerSearchRequest();
        request.setKeyword(" portrait ");
        request.setPage(1);

        Page<PhotographerPublicDto> result = publicPhotographerService.searchPhotographers(request, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getNumber()).isEqualTo(1);
    }

    @Test
    void searchPhotographers_paged_shouldRejectUnboundedPageSize() {
        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.searchPhotographers(
                        new PhotographerSearchRequest(), PageRequest.of(0, 25)));

        verify(photographerProfileRepository, never()).searchApprovedPhotographersPaged(
                any(), any(), any(), any(), any(), any(), any());
    }

    // ── getApprovedPhotographerById() ───────────────────────────────────────

    @Test
    void getById_approvedProfile_shouldReturnDto() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                10L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.of(approvedProfile));

        PhotographerPublicDto dto = publicPhotographerService.getApprovedPhotographerById(10L);

        assertThat(dto).isNotNull();
        assertThat(dto.getDisplayName()).isEqualTo("Studio Sáng Tạo");
        assertThat(dto.getCity()).isEqualTo("Hanoi");
        assertThat(dto.getExperienceYears()).isEqualTo(5);
        assertThat(dto.getPriceFrom()).isEqualByComparingTo(new BigDecimal("3000000"));
    }

    /**
     * Direct URL guessing with a PENDING id must fail cleanly.
     * The service throws because the repository returns empty for non-APPROVED.
     */
    @Test
    void getById_pendingProfile_shouldThrowIllegalArgumentException() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                2L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.getApprovedPhotographerById(2L));
    }

    @Test
    void getById_rejectedProfile_shouldThrowIllegalArgumentException() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                3L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.getApprovedPhotographerById(3L));
    }

    @Test
    void getById_suspendedProfile_shouldThrowIllegalArgumentException() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                4L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.getApprovedPhotographerById(4L));
    }

    @Test
    void getById_nonExistentId_shouldThrowIllegalArgumentException() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                999L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> publicPhotographerService.getApprovedPhotographerById(999L));
    }

    // ── DTO safety ──────────────────────────────────────────────────────────

    /**
     * The public DTO must not expose password, email, or any sensitive field.
     */
    @Test
    void publicDto_shouldNotHavePasswordOrEmailAccessors() {
        Class<PhotographerPublicDto> dtoClass = PhotographerPublicDto.class;

        boolean hasPassword = java.util.Arrays.stream(dtoClass.getMethods())
                .anyMatch(m -> m.getName().equalsIgnoreCase("getPassword"));
        boolean hasEmail = java.util.Arrays.stream(dtoClass.getMethods())
                .anyMatch(m -> m.getName().equalsIgnoreCase("getEmail"));

        assertThat(hasPassword)
                .as("PhotographerPublicDto must NOT expose a getPassword() method")
                .isFalse();
        assertThat(hasEmail)
                .as("PhotographerPublicDto must NOT expose a getEmail() method")
                .isFalse();
    }

    /**
     * DTO mapping from approved profile must correctly populate all public fields.
     */
    @Test
    void publicDto_fromApprovedProfile_shouldMapAllPublicFields() {
        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                10L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.of(approvedProfile));

        PhotographerPublicDto dto = publicPhotographerService.getApprovedPhotographerById(10L);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDisplayName()).isEqualTo("Studio Sáng Tạo");
        assertThat(dto.getBio()).isEqualTo("Professional wedding and portrait photographer based in Hanoi.");
        assertThat(dto.getCity()).isEqualTo("Hanoi");
        assertThat(dto.getExperienceYears()).isEqualTo(5);
        assertThat(dto.getPriceFrom()).isEqualByComparingTo(new BigDecimal("3000000"));
    }

    @Test
    void publicDto_withProfessionalFields_shouldMapAllProfessionalData() {
        approvedProfile.setHeadline("Editorial Fine Art");
        approvedProfile.setCountry("Vietnam");
        approvedProfile.setSpecialties("PORTRAIT,WEDDING");
        approvedProfile.setWebsiteUrl("https://studio.art");
        approvedProfile.setInstagramUrl("https://instagram.com/studio");
        approvedProfile.setFacebookUrl("https://facebook.com/studio");
        approvedProfile.setEquipmentSummary("Sony A7IV, 35mm GM");
        approvedProfile.setLanguages("English, Vietnamese");
        approvedProfile.setTravelAvailable(true);

        when(photographerProfileRepository.findByIdAndVerificationStatusWithUser(
                10L, PhotographerVerificationStatus.APPROVED))
                .thenReturn(Optional.of(approvedProfile));

        PhotographerPublicDto dto = publicPhotographerService.getApprovedPhotographerById(10L);

        assertThat(dto.getHeadline()).isEqualTo("Editorial Fine Art");
        assertThat(dto.getCountry()).isEqualTo("Vietnam");
        assertThat(dto.getSpecialties()).isEqualTo("PORTRAIT,WEDDING");
        assertThat(dto.getSpecialtyDisplayNames()).containsExactly("Portrait", "Wedding");
        assertThat(dto.getWebsiteUrl()).isEqualTo("https://studio.art");
        assertThat(dto.getInstagramUrl()).isEqualTo("https://instagram.com/studio");
        assertThat(dto.getFacebookUrl()).isEqualTo("https://facebook.com/studio");
        assertThat(dto.getEquipmentSummary()).isEqualTo("Sony A7IV, 35mm GM");
        assertThat(dto.getLanguages()).isEqualTo("English, Vietnamese");
        assertThat(dto.isTravelAvailable()).isTrue();
    }
}
