package com.photoconnect.controller;

import com.photoconnect.dto.RegisterRequest;
import com.photoconnect.exception.EmailAlreadyExistsException;
import com.photoconnect.exception.PasswordMismatchException;
import com.photoconnect.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterController.class)
public class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    public void testProcessRegistration_Valid() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "John Doe")
                        .param("email", "john.doe@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register?success"));

        Mockito.verify(userService, Mockito.times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    public void testProcessRegistration_ValidationError() throws Exception {
        // Missing name and password too short
        mockMvc.perform(post("/register")
                        .param("email", "invalid-email")
                        .param("password", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        Mockito.verify(userService, Mockito.never()).registerUser(any());
    }

    @Test
    public void testProcessRegistration_PasswordMismatch() throws Exception {
        Mockito.when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new PasswordMismatchException("Passwords do not match"));

        mockMvc.perform(post("/register")
                        .param("fullName", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password456"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("passwordError"));
    }
    
    @Test
    public void testProcessRegistration_DuplicateEmail() throws Exception {
        Mockito.when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered"));

        mockMvc.perform(post("/register")
                        .param("fullName", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("emailError"));
    }
}
