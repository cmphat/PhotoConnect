package com.photoconnect.controller;

import com.photoconnect.dto.LoginRequest;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.InvalidCredentialsException;
import com.photoconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private User validUser;

    @BeforeEach
    public void setup() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setFullName("Test User");
        validUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    public void testLoginWithValidationErrors() throws Exception {
        mockMvc.perform(post("/login")
                .param("email", "invalidemail")
                .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("loginRequest", "email", "password"));
    }

    @Test
    public void testLoginWithWrongCredentials() throws Exception {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Email or password is incorrect."));

        mockMvc.perform(post("/login")
                .param("email", "wrong@example.com")
                .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("authError"))
                .andExpect(request().sessionAttributeDoesNotExist("userId"));
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(validUser);

        mockMvc.perform(post("/login")
                .param("email", "test@example.com")
                .param("password", "correctpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute("userId", 1L))
                .andExpect(request().sessionAttribute("userEmail", "test@example.com"))
                .andExpect(request().sessionAttribute("userFullName", "Test User"))
                .andExpect(request().sessionAttribute("userRole", "CUSTOMER"));
    }

    @Test
    public void testLogout() throws Exception {
        // Need to create a session first, then assert it gets invalidated
        mockMvc.perform(post("/logout").sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
