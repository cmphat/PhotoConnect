package com.photoconnect.controller;

import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.AuditOutcome;
import com.photoconnect.entity.OperationalAuditLog;
import com.photoconnect.entity.PlatformFeeStatus;
import com.photoconnect.entity.User;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.OperationalAuditLogRepository;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.service.PlatformFeeService;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/platform-fees")
public class AdminPlatformFeeController {

    private final BookingRepository bookingRepository;
    private final OperationalAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private PlatformFeeService platformFeeService;

    // Default authoritative rate: 10%
    private static volatile BigDecimal currentPlatformFeeRate = new BigDecimal("0.10");

    @Autowired
    public AdminPlatformFeeController(BookingRepository bookingRepository,
                                      OperationalAuditLogRepository auditLogRepository,
                                      UserRepository userRepository,
                                      @Autowired(required = false) PlatformFeeService platformFeeService) {
        this.bookingRepository = bookingRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.platformFeeService = platformFeeService;
        if (platformFeeService != null) {
            currentPlatformFeeRate = platformFeeService.getCurrentFeeRate();
        }
    }

    public AdminPlatformFeeController(BookingRepository bookingRepository,
                                      OperationalAuditLogRepository auditLogRepository,
                                      UserRepository userRepository) {
        this(bookingRepository, auditLogRepository, userRepository, null);
    }

    public static BigDecimal getCurrentPlatformFeeRate() {
        return currentPlatformFeeRate;
    }

    @GetMapping
    public String viewPlatformFees(@RequestParam(value = "status", required = false) PlatformFeeStatus statusFilter,
                                   HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        BigDecimal completedRev = bookingRepository.calculateTotalPlatformCompletedRevenue();
        BigDecimal completedRevenue = completedRev != null ? completedRev : BigDecimal.ZERO;

        BigDecimal feeIncurred;
        BigDecimal feeCollected;
        BigDecimal feeOutstanding;
        long photographersWithDebt = 0;

        if (platformFeeService != null) {
            Map<String, Object> stats = platformFeeService.getAdminFeeStats();
            feeIncurred = (BigDecimal) stats.get("totalIncurred");
            feeCollected = (BigDecimal) stats.get("totalPaid");
            feeOutstanding = (BigDecimal) stats.get("totalOutstanding");
            photographersWithDebt = ((Number) stats.get("photographersWithDebt")).longValue();
            currentPlatformFeeRate = platformFeeService.getCurrentFeeRate();
        } else {
            feeIncurred = completedRevenue.multiply(currentPlatformFeeRate).setScale(0, RoundingMode.HALF_UP);
            feeCollected = BigDecimal.ZERO;
            feeOutstanding = feeIncurred.subtract(feeCollected);
        }

        List<PlatformFeeDto> feeRecords = platformFeeService != null
                ? platformFeeService.listAllFees(statusFilter)
                : List.of();

        List<Object[]> rawSummaries = bookingRepository.summarizePhotographerCompletedRevenue();
        List<PhotographerFeeSummary> photographerFees = new ArrayList<>();

        for (Object[] row : rawSummaries) {
            Long profileId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String email = (String) row[2];
            long count = ((Number) row[3]).longValue();
            BigDecimal rev = row[4] instanceof BigDecimal b ? b : BigDecimal.ZERO;
            BigDecimal fee = rev.multiply(currentPlatformFeeRate).setScale(0, RoundingMode.HALF_UP);

            photographerFees.add(new PhotographerFeeSummary(
                    profileId,
                    name != null ? name : "Photographer #" + profileId,
                    email != null ? email : "",
                    count,
                    rev,
                    fee,
                    BigDecimal.ZERO,
                    fee
            ));
        }

        model.addAttribute("completedRevenue", completedRevenue);
        model.addAttribute("platformFeeRatePercent", currentPlatformFeeRate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
        model.addAttribute("platformFeeIncurred", feeIncurred);
        model.addAttribute("platformFeeCollected", feeCollected);
        model.addAttribute("platformFeeOutstanding", feeOutstanding);
        model.addAttribute("photographersWithDebt", photographersWithDebt);
        model.addAttribute("photographerFees", photographerFees);
        model.addAttribute("feeRecords", feeRecords);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("statuses", PlatformFeeStatus.values());
        model.addAttribute("activeTab", "platform-fees");

        return "admin-platform-fees";
    }

    @PostMapping("/rate")
    public String updatePlatformFeeRate(@RequestParam("ratePercent") BigDecimal ratePercent,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        Long adminId = (Long) session.getAttribute("userId");
        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;

        if (ratePercent == null || ratePercent.compareTo(BigDecimal.ZERO) < 0 || ratePercent.compareTo(new BigDecimal("30")) > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tỷ lệ phí sàn không hợp lệ. Phải từ 0% đến 30%.");
            return "redirect:/admin/platform-fees";
        }

        BigDecimal newRate = ratePercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal oldRate = currentPlatformFeeRate;
        currentPlatformFeeRate = newRate;

        if (platformFeeService != null) {
            platformFeeService.updateFeeRate(newRate, adminId);
        } else if (admin != null) {
            auditLogRepository.save(new OperationalAuditLog(
                    admin,
                    "UPDATE_PLATFORM_FEE_RATE",
                    "SYSTEM_SETTING",
                    "PLATFORM_FEE",
                    AuditOutcome.SUCCESS,
                    "Updated platform fee commission rate from " + oldRate.multiply(new BigDecimal(100)) + "% to " + ratePercent + "%"
            ));
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tỷ lệ phí sàn thành công sang " + ratePercent + "%. Áp dụng cho các giao dịch mới.");
        return "redirect:/admin/platform-fees";
    }

    @GetMapping("/export")
    public void exportFeesCsv(@RequestParam(value = "status", required = false) PlatformFeeStatus statusFilter,
                              HttpSession session,
                              HttpServletResponse response) throws IOException {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            response.sendRedirect(redirect);
            return;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"platform-fees.csv\"");

        List<PlatformFeeDto> feeRecords = platformFeeService != null
                ? platformFeeService.listAllFees(statusFilter)
                : List.of();

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("ID,Booking ID,Photographer,Booking Amount,Fee Rate,Fee Amount,Paid Amount,Outstanding,Status,Date,Transaction Code");

        for (PlatformFeeDto fee : feeRecords) {
            writer.printf("%d,%d,\"%s\",%s,%s,%s,%s,%s,%s,\"%s\",\"%s\"%n",
                    fee.getId() != null ? fee.getId() : 0,
                    fee.getBookingId() != null ? fee.getBookingId() : 0,
                    fee.getPhotographerName() != null ? fee.getPhotographerName().replace("\"", "\"\"") : "",
                    fee.getBookingAmount() != null ? fee.getBookingAmount().toPlainString() : "0",
                    fee.getFeeRatePercent() != null ? fee.getFeeRatePercent() : "10%",
                    fee.getFeeAmount() != null ? fee.getFeeAmount().toPlainString() : "0",
                    fee.getPaidAmount() != null ? fee.getPaidAmount().toPlainString() : "0",
                    fee.getOutstandingAmount() != null ? fee.getOutstandingAmount().toPlainString() : "0",
                    fee.getStatus() != null ? fee.getStatus().name() : "",
                    fee.getFormattedCreatedAt() != null ? fee.getFormattedCreatedAt() : "",
                    fee.getTransactionCode() != null ? fee.getTransactionCode() : ""
            );
        }
        writer.flush();
    }

    public static class PhotographerFeeSummary {
        private final Long profileId;
        private final String displayName;
        private final String email;
        private final long completedBookingsCount;
        private final BigDecimal totalRevenue;
        private final BigDecimal feeDue;
        private final BigDecimal feePaid;
        private final BigDecimal feeOutstanding;

        public PhotographerFeeSummary(Long profileId, String displayName, String email,
                                      long completedBookingsCount, BigDecimal totalRevenue,
                                      BigDecimal feeDue, BigDecimal feePaid, BigDecimal feeOutstanding) {
            this.profileId = profileId;
            this.displayName = displayName;
            this.email = email;
            this.completedBookingsCount = completedBookingsCount;
            this.totalRevenue = totalRevenue;
            this.feeDue = feeDue;
            this.feePaid = feePaid;
            this.feeOutstanding = feeOutstanding;
        }

        public Long getProfileId() { return profileId; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public long getCompletedBookingsCount() { return completedBookingsCount; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getFeeDue() { return feeDue; }
        public BigDecimal getFeePaid() { return feePaid; }
        public BigDecimal getFeeOutstanding() { return feeOutstanding; }
    }
}
