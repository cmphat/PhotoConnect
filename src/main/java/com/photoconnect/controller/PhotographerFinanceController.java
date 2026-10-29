package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerFeeSummaryDto;
import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PlatformFeeService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class PhotographerFinanceController {

    private final PlatformFeeService platformFeeService;

    @Autowired
    public PhotographerFinanceController(PlatformFeeService platformFeeService) {
        this.platformFeeService = platformFeeService;
    }

    @GetMapping({"/photographer/finance", "/photographer/platform-fees"})
    public String viewFinance(HttpSession session, Model model) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }

        Long userId = SessionSecurityUtils.userId(session);
        PhotographerFeeSummaryDto summary = platformFeeService.getPhotographerSummary(userId);

        model.addAttribute("summary", summary);
        model.addAttribute("activeNav", "finance");
        return "photographer-finance";
    }

    @PostMapping("/photographer/platform-fees/{feeId}/pay")
    public String payPlatformFee(@PathVariable("feeId") Long feeId,
                                 @RequestParam(value = "amount", required = false) BigDecimal amount,
                                 @RequestParam(value = "paymentMethod", defaultValue = "DEMO_PAYMENT") String paymentMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) {
            return redirect;
        }

        Long userId = SessionSecurityUtils.userId(session);

        try {
            // If amount not explicitly given, pay full outstanding debt on this fee record
            if (amount == null) {
                PhotographerFeeSummaryDto summary = platformFeeService.getPhotographerSummary(userId);
                PlatformFeeDto targetFee = summary.getFees().stream()
                        .filter(f -> f.getId().equals(feeId))
                        .findFirst()
                        .orElse(null);
                if (targetFee != null) {
                    amount = targetFee.getOutstandingAmount();
                } else {
                    amount = BigDecimal.ZERO;
                }
            }

            PlatformFeeDto paidFee = platformFeeService.processPhotographerFeePayment(userId, feeId, amount, paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán phí sàn thành công! Mã giao dịch: " + paidFee.getTransactionCode() + " (" + paidFee.getFormattedPaidAmount() + ")");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại: " + ex.getMessage());
        }

        return "redirect:/photographer/finance";
    }
}
