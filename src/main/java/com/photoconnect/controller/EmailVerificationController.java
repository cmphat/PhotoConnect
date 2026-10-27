package com.photoconnect.controller;

import com.photoconnect.exception.EmailDeliveryException;
import com.photoconnect.service.UserService;
import com.photoconnect.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailVerificationController {

    public static final String PENDING_REGISTRATION_EMAIL_SESSION_KEY = "pendingRegistrationEmail";
    private static final String INVALID_VERIFICATION_CONTEXT_MESSAGE =
            "Phiên xác thực không hợp lệ hoặc đã hết hạn. Vui lòng đăng ký lại.";

    private final UserService userService;

    public EmailVerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(
            @RequestParam(name = "resent", required = false) String resent,
            @RequestParam(name = "sent", required = false) String sent,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        String email = getPendingRegistrationEmail(session);
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("purpose", "REGISTRATION");
        if (email == null) {
            model.addAttribute("errorMessage", INVALID_VERIFICATION_CONTEXT_MESSAGE);
        } else if ("true".equals(sent)) {
            model.addAttribute("infoMessage", "Mã xác thực đã được gửi tới email của bạn.");
        }
        if ("true".equals(resent)) {
            model.addAttribute("infoMessage", "Mã xác thực mới đã được gửi tới email của bạn.");
        }

        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @RequestParam("otpCode") String otpCode,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        String email = getPendingRegistrationEmail(session);
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("purpose", "REGISTRATION");

        if (email == null) {
            model.addAttribute("errorMessage", INVALID_VERIFICATION_CONTEXT_MESSAGE);
            return "verify-otp";
        }

        try {
            userService.verifyRegistrationOtp(email, otpCode);
            session.removeAttribute(PENDING_REGISTRATION_EMAIL_SESSION_KEY);
            return "redirect:/login?verified=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        String email = getPendingRegistrationEmail(session);
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("purpose", "REGISTRATION");

        if (email == null) {
            model.addAttribute("errorMessage", INVALID_VERIFICATION_CONTEXT_MESSAGE);
            return "verify-otp";
        }

        try {
            String clientIp = ClientIpUtils.getClientIp(request);
            userService.resendRegistrationOtp(email, clientIp);
            return "redirect:/verify-otp?resent=true";
        } catch (EmailDeliveryException e) {
            model.addAttribute("errorMessage",
                    "Không thể gửi email xác thực lúc này. Vui lòng thử lại sau.");
            return "verify-otp";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "verify-otp";
        }
    }

    private String getPendingRegistrationEmail(HttpSession session) {
        Object value = session.getAttribute(PENDING_REGISTRATION_EMAIL_SESSION_KEY);
        if (!(value instanceof String email) || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
