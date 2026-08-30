package com.photoconnect.controller;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.exception.InvalidCredentialsException;
import com.photoconnect.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
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
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            User user = authService.authenticate(loginRequest);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("userRole", user.getRole().name());
            
            return "redirect:/";
        } catch (InvalidCredentialsException | AccountDisabledException e) {
            model.addAttribute("authError", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
