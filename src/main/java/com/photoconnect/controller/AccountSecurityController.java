package com.photoconnect.controller;

import com.photoconnect.security.JwtCookieService;
import com.photoconnect.service.UserService;
import com.photoconnect.util.ClientIpUtils;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/profile/security")
public class AccountSecurityController {

    private final UserService userService;
    private final JwtCookieService jwtCookieService;

    public AccountSecurityController(UserService userService, JwtCookieService jwtCookieService) {
        this.userService = userService;
        this.jwtCookieService = jwtCookieService;
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            userService.changePassword(currentUserId, currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("securitySuccess", "Đổi mật khẩu thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("securityError", e.getMessage());
        }

        return "redirect:/profile/me#security";
    }

    @PostMapping("/email/request")
    public String requestEmailChange(
            @RequestParam("newEmail") String newEmail,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            String clientIp = ClientIpUtils.getClientIp(request);
            userService.requestEmailChange(currentUserId, newEmail, clientIp);
            redirectAttributes.addFlashAttribute("emailChangePending", true);
            redirectAttributes.addFlashAttribute("targetNewEmail", newEmail.trim().toLowerCase());
            redirectAttributes.addFlashAttribute("securitySuccess",
                    "Mã xác thực OTP đã được gửi tới email hiện tại của bạn. Vui lòng nhập mã để hoàn tất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("securityError", e.getMessage());
        }

        return "redirect:/profile/me#security";
    }

    @PostMapping("/email/confirm")
    public String confirmEmailChange(
            @RequestParam("otpCode") String otpCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            userService.confirmEmailChange(currentUserId, otpCode);
            redirectAttributes.addFlashAttribute("securitySuccess", "Cập nhật địa chỉ email thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("securityError", e.getMessage());
        }

        return "redirect:/profile/me#security";
    }

    @PostMapping("/deactivate")
    public String deactivateAccount(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        try {
            userService.deactivateAccount(currentUserId);
            jwtCookieService.clear(request, response);
            session.invalidate();
            return "redirect:/login?deactivated=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("securityError", e.getMessage());
            return "redirect:/profile/me#security";
        }
    }
}
