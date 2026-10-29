package com.photoconnect.service;

import com.photoconnect.dto.PhotographerEvaluationDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Photographer Application Evaluation (6 Criteria Screening) Test")
class PhotographerEvaluationServiceTest {

    private PhotographerEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new PhotographerEvaluationServiceImpl();
    }

    @Test
    @DisplayName("Null profile returns 0 score with safe fallback")
    void evaluate_nullProfile_returnsZeroScore() {
        PhotographerEvaluationDto result = evaluationService.evaluate(null);
        assertThat(result.getTotalScore()).isEqualTo(0);
        assertThat(result.getCompletenessLevel()).contains("Không có hồ sơ");
        assertThat(result.getBadgeClass()).isEqualTo("danger");
    }

    @Test
    @DisplayName("Complete profile achieves high score (>=80%) and recommends manual verification")
    void evaluate_completeProfile_recommendsManualVerification() {
        User user = new User();
        user.setEmail("alex.nguyen@example.com");
        user.setFullName("Nguyễn Văn Alex");
        user.setPhone("0901234567");
        user.setAvatarUrl("https://cloudinary.com/avatar.jpg");
        user.setRole(UserRole.CUSTOMER);

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("Alex Nguyen Photography");
        profile.setHeadline("Editorial & Portrait Photographer");
        profile.setSpecialties("Chân dung nghệ thuật, Editorial, Sự kiện cưới cao cấp");
        profile.setBio("Hơn 5 năm kinh nghiệm chụp chân dung nghệ thuật và thời trang tại TP.HCM. Cam kết hình ảnh chất lượng cao và quy trình làm việc chuẩn mực.");
        profile.setEquipmentSummary("Sony A7IV, 24-70mm f/2.8 GM II, 85mm f/1.4 GM, Godox V1");
        profile.setCity("Hồ Chí Minh");
        profile.setCountry("Việt Nam");
        profile.setExperienceYears(5);
        profile.setLanguages("Tiếng Việt, Tiếng Anh");
        profile.setPriceFrom(new BigDecimal("1500000"));
        profile.setTravelAvailable(true);
        profile.setWebsiteUrl("https://alexnguyen.photo");
        profile.setInstagramUrl("https://instagram.com/alexnguyen.photo");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        PhotographerEvaluationDto result = evaluationService.evaluate(profile);

        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(85);
        assertThat(result.getBadgeClass()).isEqualTo("success");
        assertThat(result.getRecommendation()).contains("Chuyển sang bước kiểm tra thủ công");
        assertThat(result.getCriteria()).hasSize(6);
        assertThat(result.getSatisfiedItems()).isNotEmpty();
        assertThat(result.getManualVerificationPoints()).isNotEmpty();
    }

    @Test
    @DisplayName("Sparse profile identifies missing criteria transparently")
    void evaluate_sparseProfile_identifiesMissingFields() {
        User user = new User();
        user.setEmail("sparse@example.com");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("Test Photo");
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);

        PhotographerEvaluationDto result = evaluationService.evaluate(profile);

        assertThat(result.getTotalScore()).isLessThan(55);
        assertThat(result.getBadgeClass()).isEqualTo("danger");
        assertThat(result.getRecommendation()).contains("bổ sung");
        assertThat(result.getMissingItems()).contains(
                "Chưa cập nhật ảnh đại diện cá nhân/studio",
                "Chưa có số điện thoại liên lạc",
                "Chưa cung cấp danh sách thiết bị máy ảnh, ống kính, ánh sáng"
        );
    }

    @Test
    @DisplayName("Guarantees that pending applicant is NOT penalized for missing internal portfolio")
    void evaluate_doesNotPenalizeForMissingInternalPortfolio() {
        User user = new User();
        user.setEmail("pro@example.com");
        user.setPhone("0912345678");
        user.setAvatarUrl("https://cloudinary.com/avatar.jpg");

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setDisplayName("Pro Studio");
        profile.setHeadline("Commercial Photographer");
        profile.setSpecialties("Sản phẩm, Thương mại");
        profile.setBio("Chuyên chụp sản phẩm thương mại và thương mại điện tử chuyên nghiệp tại Hà Nội.");
        profile.setEquipmentSummary("Canon R5, Canon RF 50mm f/1.2L, Đèn studio Jinbei");
        profile.setCity("Hà Nội");
        profile.setExperienceYears(3);
        profile.setLanguages("Tiếng Việt");
        profile.setPriceFrom(new BigDecimal("2000000"));
        profile.setWebsiteUrl("https://prostudio.vn");
        profile.setInstagramUrl("https://instagram.com/prostudio.vn");

        PhotographerEvaluationDto result = evaluationService.evaluate(profile);

        // Score must not require portfolioImage entities or deduct points for empty gallery
        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(90);
    }
}
