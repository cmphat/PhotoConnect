package com.photoconnect.service;

import com.photoconnect.dto.PhotographerEvaluationDto;
import com.photoconnect.dto.PhotographerEvaluationDto.CriterionItem;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PhotographerEvaluationServiceImpl implements PhotographerEvaluationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AI_SERVICE_URL = "http://localhost:8000/api/ai/evaluate-photographer";

    @Override
    public PhotographerEvaluationDto evaluate(PhotographerProfile profile) {
        PhotographerEvaluationDto dto = new PhotographerEvaluationDto();
        if (profile == null) {
            dto.setTotalScore(0);
            dto.setCompletenessLevel("Không có hồ sơ");
            dto.setBadgeClass("danger");
            dto.setRecommendation("Không tìm thấy thông tin hồ sơ ứng viên.");
            return dto;
        }

        User user = profile.getUser();
        List<CriterionItem> criteria = new ArrayList<>();
        List<String> satisfiedItems = new ArrayList<>();
        List<String> missingItems = new ArrayList<>();
        List<String> manualVerificationPoints = new ArrayList<>();

        int totalScore = 0;

        // ── 1. Mức độ đầy đủ thông tin hồ sơ (25%) ──────────────────────────────
        int c1Earned = 0;
        CriterionItem c1 = new CriterionItem("Mức độ đầy đủ thông tin hồ sơ", 25, 0, false);
        if (isNotBlank(profile.getDisplayName())) {
            c1Earned += 5;
            c1.getMetDetails().add("Tên hiển thị: " + profile.getDisplayName().trim());
        } else {
            c1.getMissingDetails().add("Chưa có tên hiển thị chuyên nghiệp");
        }

        if (user != null && isNotBlank(user.getAvatarUrl())) {
            c1Earned += 5;
            c1.getMetDetails().add("Đã có ảnh đại diện hồ sơ");
        } else {
            c1.getMissingDetails().add("Chưa cập nhật ảnh đại diện cá nhân/studio");
        }

        if (user != null && isNotBlank(user.getPhone())) {
            c1Earned += 5;
            c1.getMetDetails().add("Số điện thoại liên hệ: " + user.getPhone().trim());
        } else {
            c1.getMissingDetails().add("Chưa có số điện thoại liên lạc");
        }

        if (user != null && isNotBlank(user.getEmail())) {
            c1Earned += 5;
            c1.getMetDetails().add("Email xác thực: " + user.getEmail().trim());
        } else {
            c1.getMissingDetails().add("Thiếu địa chỉ email");
        }

        if (isNotBlank(profile.getBio())) {
            c1Earned += 5;
            c1.getMetDetails().add("Đã có đoạn giới thiệu tóm tắt");
        } else {
            c1.getMissingDetails().add("Chưa có mô tả tiểu sử/giới thiệu");
        }

        c1.setEarnedScore(c1Earned);
        c1.setMet(c1Earned >= 20);
        criteria.add(c1);
        totalScore += c1Earned;
        manualVerificationPoints.add("Xác thực thông tin liên lạc (gọi điện hoặc gửi email trao đổi sơ bộ nếu cần).");

        // ── 2. Mô tả chuyên môn và dịch vụ (20%) ─────────────────────────────────
        int c2Earned = 0;
        CriterionItem c2 = new CriterionItem("Mô tả chuyên môn và dịch vụ", 20, 0, false);
        if (isNotBlank(profile.getHeadline())) {
            c2Earned += 7;
            c2.getMetDetails().add("Tiêu đề nghề nghiệp: " + profile.getHeadline().trim());
        } else {
            c2.getMissingDetails().add("Thiếu tiêu đề định vị phong cách (headline)");
        }

        if (isNotBlank(profile.getSpecialties())) {
            c2Earned += 7;
            c2.getMetDetails().add("Chuyên môn chụp: " + profile.getSpecialties().trim());
        } else {
            c2.getMissingDetails().add("Chưa liệt kê các thể loại chụp ảnh chuyên môn");
        }

        if (isNotBlank(profile.getBio()) && profile.getBio().trim().length() >= 50) {
            c2Earned += 6;
            c2.getMetDetails().add("Mô tả dịch vụ chi tiết (" + profile.getBio().trim().length() + " ký tự)");
        } else {
            c2.getMissingDetails().add("Mô tả dịch vụ còn ngắn hoặc sơ sài (dưới 50 ký tự)");
        }

        c2.setEarnedScore(c2Earned);
        c2.setMet(c2Earned >= 14);
        criteria.add(c2);
        totalScore += c2Earned;
        manualVerificationPoints.add("Kiểm tra phong cách nghệ thuật và cam kết dịch vụ có phù hợp định hướng sàn.");

        // ── 3. Thông tin kinh nghiệm (15%) ──────────────────────────────────────
        int c3Earned = 0;
        CriterionItem c3 = new CriterionItem("Thông tin kinh nghiệm", 15, 0, false);
        if (profile.getExperienceYears() != null && profile.getExperienceYears() >= 1) {
            c3Earned += 10;
            c3.getMetDetails().add("Số năm kinh nghiệm: " + profile.getExperienceYears() + " năm");
        } else {
            c3.getMissingDetails().add("Chưa khai báo hoặc kinh nghiệm dưới 1 năm");
        }

        if (isNotBlank(profile.getLanguages())) {
            c3Earned += 5;
            c3.getMetDetails().add("Ngôn ngữ giao tiếp: " + profile.getLanguages().trim());
        } else {
            c3.getMissingDetails().add("Chưa khai báo ngoại ngữ giao tiếp");
        }

        c3.setEarnedScore(c3Earned);
        c3.setMet(c3Earned >= 10);
        criteria.add(c3);
        totalScore += c3Earned;
        manualVerificationPoints.add("Kiểm chứng kinh nghiệm tác nghiệp thông qua các dự án đã thực hiện.");

        // ── 4. Thông tin thiết bị (15%) ─────────────────────────────────────────
        int c4Earned = 0;
        CriterionItem c4 = new CriterionItem("Thông tin thiết bị", 15, 0, false);
        boolean hasDslrGear = false;
        if (isNotBlank(profile.getEquipmentSummary())) {
            String eq = profile.getEquipmentSummary().trim();
            String eqLower = eq.toLowerCase();
            if (eqLower.contains("nikon") || eqLower.contains("canon") || eqLower.contains("sony")
                    || eqLower.contains("dslr") || eqLower.contains("mirrorless")) {
                hasDslrGear = true;
            }
            if (eq.length() >= 15) {
                c4Earned = 15;
                c4.getMetDetails().add("Khai báo thiết bị đầy đủ: " + eq);
            } else {
                c4Earned = 8;
                c4.getMetDetails().add("Khai báo thiết bị ngắn gọn: " + eq);
                c4.getMissingDetails().add("Nên bổ sung chi tiết máy ảnh, ống kính và thiết bị chiếu sáng");
            }
        } else {
            c4.getMissingDetails().add("Chưa cung cấp danh sách thiết bị máy ảnh, ống kính, ánh sáng");
        }

        c4.setEarnedScore(c4Earned);
        c4.setMet(c4Earned >= 10);
        criteria.add(c4);
        totalScore += c4Earned;
        manualVerificationPoints
                .add("Đánh giá hệ thống thiết bị có đảm bảo chất lượng kỹ thuật (độ phân giải, khẩu độ, ánh sáng).");

        // ── 5. Khu vực hoạt động và giá dịch vụ (15%) ────────────────────────────
        int c5Earned = 0;
        CriterionItem c5 = new CriterionItem("Khu vực hoạt động và giá dịch vụ", 15, 0, false);
        if (isNotBlank(profile.getCity())) {
            c5Earned += 6;
            String loc = profile.getCity().trim()
                    + (isNotBlank(profile.getCountry()) ? ", " + profile.getCountry().trim() : "");
            c5.getMetDetails().add("Địa bàn hoạt động chính: " + loc);
        } else {
            c5.getMissingDetails().add("Chưa khai báo thành phố/khu vực tác nghiệp chính");
        }

        if (profile.getPriceFrom() != null && profile.getPriceFrom().compareTo(BigDecimal.ZERO) > 0) {
            c5Earned += 6;
            c5.getMetDetails().add("Giá khởi điểm rõ ràng: " + String.format("%,.0f VND", profile.getPriceFrom()));
        } else {
            c5.getMissingDetails().add("Chưa thiết lập mức giá khởi điểm (priceFrom)");
        }

        if (profile.isTravelAvailable()) {
            c5Earned += 3;
            c5.getMetDetails().add("Sẵn sàng di chuyển ngoại tỉnh/xa theo yêu cầu khách hàng");
        } else {
            c5Earned += 3;
            c5.getMetDetails().add("Phạm vi hoạt động cố định tại khu vực đã đăng ký");
        }

        c5.setEarnedScore(c5Earned);
        c5.setMet(c5Earned >= 12);
        criteria.add(c5);
        totalScore += c5Earned;
        manualVerificationPoints.add("Đối chiếu mức giá khởi điểm có hợp lý với phân khúc thị trường địa phương.");

        // ── 6. Tài liệu nghề nghiệp tự nguyện cung cấp (10%) ─────────────────────
        int c6Earned = 0;
        CriterionItem c6 = new CriterionItem("Tài liệu nghề nghiệp tự nguyện cung cấp", 10, 0, false);
        int linkCount = 0;
        if (isNotBlank(profile.getWebsiteUrl()))
            linkCount++;
        if (isNotBlank(profile.getInstagramUrl()))
            linkCount++;
        if (isNotBlank(profile.getFacebookUrl()))
            linkCount++;
        if (isNotBlank(profile.getTiktokUrl()))
            linkCount++;
        if (isNotBlank(profile.getYoutubeUrl()))
            linkCount++;

        if (linkCount >= 2) {
            c6Earned = 10;
            c6.getMetDetails().add("Cung cấp " + linkCount + " liên kết tham khảo ngoài (Website/Mạng xã hội)");
        } else if (linkCount == 1) {
            c6Earned = 7;
            c6.getMetDetails().add("Cung cấp 1 liên kết tham khảo ngoài");
            c6.getMissingDetails().add("Có thể bổ sung thêm Website hoặc Instagram để tăng độ tin cậy");
        } else {
            c6.getMissingDetails().add("Chưa cung cấp liên kết website/portfolio bên ngoài để tham khảo");
        }

        c6.setEarnedScore(c6Earned);
        c6.setMet(c6Earned >= 7);
        criteria.add(c6);
        totalScore += c6Earned;
        manualVerificationPoints
                .add("Mở liên kết portfolio ngoài để thẩm định chất lượng hình ảnh thực tế và bản quyền tác phẩm.");

        // ── 7. Gọi Model AI Flickr (FastAPI) thẩm định chuyên sâu ────────────────
        String aiRecommendationText = "";
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("photos_count", 15);
            req.put("views_count", 500);
            req.put("favorites_count", 40);
            req.put("has_dslr_gear", hasDslrGear);
            req.put("years_of_experience", profile.getExperienceYears() != null ? profile.getExperienceYears() : 1);
            req.put("bio", profile.getBio() != null ? profile.getBio() : "");

            Map<?, ?> aiResponse = restTemplate.postForObject(AI_SERVICE_URL, req, Map.class);
            if (aiResponse != null && aiResponse.containsKey("recommendation")) {
                String aiDecision = (String) aiResponse.get("recommendation");
                Object confidence = aiResponse.get("confidence_score");
                String reason = (String) aiResponse.get("reason");
                aiRecommendationText = " [AI Flickr Model: "
                        + ("APPROVED".equalsIgnoreCase(aiDecision) ? "ĐỀ XUẤT DUYỆT" : "CÂN NHẮC TỪ CHỐI")
                        + " - Độ tin cậy: " + confidence + "%. Lý do: " + reason + "]";
            }
        } catch (Exception ignored) {
            // Server AI Python chưa bật thì ứng dụng vẫn tiếp tục chạy bình thường
        }

        // ── Tổng hợp và Đề xuất ─────────────────────────────────────────────────
        dto.setTotalScore(totalScore);
        dto.setCriteria(criteria);
        dto.setManualVerificationPoints(manualVerificationPoints);

        for (CriterionItem item : criteria) {
            satisfiedItems.addAll(item.getMetDetails());
            missingItems.addAll(item.getMissingDetails());
        }
        dto.setSatisfiedItems(satisfiedItems);
        dto.setMissingItems(missingItems);

        if (totalScore >= 80) {
            dto.setCompletenessLevel("Hồ sơ rất đầy đủ (" + totalScore + "%)");
            dto.setBadgeClass("success");
            dto.setRecommendation("Khuyến nghị duyệt hồ sơ." + aiRecommendationText);
            dto.setExplanation(
                    "Ứng viên đã khai báo đầy đủ kinh nghiệm, chuyên môn, thiết bị và liên kết nghề nghiệp. Thông tin đã đáp ứng đa số tiêu chí định lượng.");
        } else if (totalScore >= 55) {
            dto.setCompletenessLevel("Hồ sơ mức độ trung bình (" + totalScore + "%)");
            dto.setBadgeClass("warning");
            dto.setRecommendation("Cần thẩm định thủ công hoặc yêu cầu bổ sung." + aiRecommendationText);
            dto.setExplanation(
                    "Hồ sơ đã có các trường cơ bản nhưng còn thiếu một số mục quan trọng (như chi tiết thiết bị, kinh nghiệm hoặc liên kết minh chứng).");
        } else {
            dto.setCompletenessLevel("Hồ sơ còn thiếu nhiều thông tin (" + totalScore + "%)");
            dto.setBadgeClass("danger");
            dto.setRecommendation("Đề xuất yêu cầu bổ sung hồ sơ hoặc từ chối." + aiRecommendationText);
            dto.setExplanation(
                    "Hồ sơ thiếu nhiều trường thông tin cốt lõi cần thiết cho một nhiếp ảnh gia hoạt động trên sàn.");
        }

        return dto;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}