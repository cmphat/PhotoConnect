package com.photoconnect.controller;

import com.photoconnect.util.AdminSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    @Value("${photoconnect.payment.simulation-enabled:true}")
    private boolean paymentSimulationEnabled;

    @Value("${photoconnect.payment.mode:DEMO}")
    private String paymentMode;

    @Value("${photoconnect.mail.capture-mode:true}")
    private boolean mailCaptureMode;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${photoconnect.oauth.google.client-id:}")
    private String googleClientId;

    @GetMapping
    public String viewSettings(HttpSession session, Model model) {
        String redirect = AdminSecurityUtils.requireAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        BigDecimal ratePercent = AdminPlatformFeeController.getCurrentPlatformFeeRate().multiply(new BigDecimal("100"));
        model.addAttribute("platformFeeRatePercent", ratePercent.stripTrailingZeros().toPlainString());
        model.addAttribute("depositRatePercent", "30");
        model.addAttribute("cancellationWindowHours", "48");
        model.addAttribute("paymentMode", paymentMode);
        model.addAttribute("paymentSimulationEnabled", paymentSimulationEnabled);
        model.addAttribute("mailCaptureMode", mailCaptureMode);
        model.addAttribute("cloudinaryConfigured", cloudinaryCloudName != null && !cloudinaryCloudName.isBlank());
        model.addAttribute("googleOAuthConfigured", googleClientId != null && !googleClientId.isBlank());
        model.addAttribute("activeTab", "settings");

        return "admin-settings";
    }
}
