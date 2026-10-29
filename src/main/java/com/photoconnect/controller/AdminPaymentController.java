package com.photoconnect.controller;

import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.repository.DepositRepository;
import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final DepositRepository depositRepository;

    public AdminPaymentController(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    @GetMapping
    public String listPayments(@RequestParam(value = "status", required = false) String statusStr,
                               HttpSession session,
                               Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        DepositStatus status = null;
        if (statusStr != null && !statusStr.isBlank() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                status = DepositStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<Deposit> deposits = (status != null)
                ? depositRepository.findByStatusWithDetails(status)
                : depositRepository.findAllWithDetails();

        BigDecimal paidAmount = depositRepository.sumAmountByStatus(DepositStatus.PAID);
        BigDecimal refundedAmount = depositRepository.sumAmountByStatus(DepositStatus.REFUNDED);
        BigDecimal forfeitedAmount = depositRepository.sumAmountByStatus(DepositStatus.FORFEITED);

        model.addAttribute("deposits", deposits);
        model.addAttribute("selectedStatus", status != null ? status.name() : "ALL");
        model.addAttribute("statuses", DepositStatus.values());
        model.addAttribute("paidAmount", paidAmount != null ? paidAmount : BigDecimal.ZERO);
        model.addAttribute("refundedAmount", refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
        model.addAttribute("forfeitedAmount", forfeitedAmount != null ? forfeitedAmount : BigDecimal.ZERO);
        model.addAttribute("totalCount", deposits.size());
        model.addAttribute("activeTab", "payments");

        return "admin-payments";
    }
}
