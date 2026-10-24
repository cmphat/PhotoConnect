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
public class PasswordRecoveryController {

    private final UserService userService;

    public PasswordRecoveryController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request,
            HttpSession session) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        String clientIp = ClientIpUtils.getClientIp(request);
        userService.requestPasswordReset(email, clientIp);

        String encodedEmail = URLEncoder.encode(email != null ? email.trim() : "", StandardCharsets.UTF_8);
        return "redirect:/reset-password?email=" + encodedEmail + "&requested=true";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "requested", required = false) String requested,
            @RequestParam(name = "resent", required = false) String resent,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        model.addAttribute("email", email != null ? email.trim() : "");
        if ("true".equals(requested)) {
            model.addAttribute("infoMessage", "Nếu email đã đăng ký, mã xác thực 6 chữ số đã được gửi tới hòm thư của bạn.");
        }
        if ("true".equals(resent)) {
            model.addAttribute("infoMessage", "Mã xác thực mới đã được gửi tới email của bạn.");
        }

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("email") String email,
            @RequestParam("otpCode") String otpCode,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        model.addAttribute("email", email);

        try {
            userService.resetPassword(email, otpCode, newPassword, confirmPassword);
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "reset-password";
        }
    }

    @PostMapping("/reset-password/resend")
    public String resendResetOtp(
            @RequestParam("email") String email,
            HttpServletRequest request,
            Model model) {

        model.addAttribute("email", email);

        try {
            String clientIp = ClientIpUtils.getClientIp(request);
            userService.requestPasswordReset(email, clientIp);
            String encodedEmail = URLEncoder.encode(email != null ? email.trim() : "", StandardCharsets.UTF_8);
            return "redirect:/reset-password?email=" + encodedEmail + "&resent=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "reset-password";
        }
    }
}
