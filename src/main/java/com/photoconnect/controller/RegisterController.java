package com.photoconnect.controller;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.exception.EmailAlreadyExistsException;
import com.photoconnect.exception.PasswordMismatchException;
import com.photoconnect.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegisterForm(Model model, HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping
    public String processRegistration(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registerRequest);
            return "redirect:/register?success";
        } catch (PasswordMismatchException e) {
            model.addAttribute("passwordError", e.getMessage());
            return "register";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("emailError", e.getMessage());
            return "register";
        }
    }
}
