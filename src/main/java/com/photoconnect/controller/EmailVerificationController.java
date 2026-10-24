package com.photoconnect.controller;

import com.photoconnect.service.UserService;
import com.photoconnect.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class EmailVerificationController {

    private final UserService userService;

    public EmailVerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "purpose", defaultValue = "REGISTRATION") String purpose,
            @RequestParam(name = "resent", required = false) String resent,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        model.addAttribute("email", email != null ? email.trim() : "");
        model.addAttribute("purpose", purpose);
        if ("true".equals(resent)) {
            model.addAttribute("infoMessage", "Mã xác thực mới đã được gửi tới email của bạn.");
        }

        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otpCode") String otpCode,
            @RequestParam(name = "purpose", defaultValue = "REGISTRATION") String purpose,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        model.addAttribute("email", email);
        model.addAttribute("purpose", purpose);

        try {
            userService.verifyRegistrationOtp(email, otpCode);
            return "redirect:/login?verified=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam("email") String email,
            @RequestParam(name = "purpose", defaultValue = "REGISTRATION") String purpose,
            HttpServletRequest request,
            Model model) {

        model.addAttribute("email", email);
        model.addAttribute("purpose", purpose);

        try {
            String clientIp = ClientIpUtils.getClientIp(request);
            userService.resendRegistrationOtp(email, clientIp);
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return "redirect:/verify-otp?email=" + encodedEmail + "&purpose=" + purpose + "&resent=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "verify-otp";
        }
    }
}
