package com.photoconnect.controller;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.InvalidCredentialsException;
import com.photoconnect.service.AuthService;
import com.photoconnect.security.JwtCookieService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    public LoginController(AuthService authService, JwtCookieService jwtCookieService) {
        this.authService = authService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                               BindingResult bindingResult,
                               HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse,
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            User user = authService.authenticate(loginRequest);
            httpRequest.changeSessionId();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("userRole", user.getRole().name());
            jwtCookieService.issue(httpRequest, httpResponse, user);

            if (session.getAttribute(GoogleAuthController.LINK_EMAIL_KEY) != null) {
                session.removeAttribute(GoogleAuthController.LINK_EMAIL_KEY);
                return "redirect:/auth/google/link";
            }
            return "redirect:/";
        } catch (InvalidCredentialsException | AccountDisabledException e) {
            model.addAttribute("authError", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        jwtCookieService.clear(request, response);
        session.invalidate();
        return "redirect:/";
    }
}
