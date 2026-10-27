package com.photoconnect.controller;

import com.photoconnect.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailVerificationController.class)
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void otpPageRetainsServerSideEmailAcrossRefresh() throws Exception {
        MockHttpSession session = pendingSession("registered@example.com");

        mockMvc.perform(get("/verify-otp").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-otp"))
                .andExpect(model().attribute("email", "registered@example.com"));

        mockMvc.perform(get("/verify-otp").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("email", "registered@example.com"));
    }

    @Test
    void resendUsesSessionEmailAndIgnoresTamperedClientEmail() throws Exception {
        MockHttpSession session = pendingSession("registered@example.com");

        mockMvc.perform(post("/resend-otp")
                        .session(session)
                        .param("email", "attacker@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify-otp?resent=true"));

        Mockito.verify(userService).resendRegistrationOtp(
                Mockito.eq("registered@example.com"), Mockito.anyString());
        Mockito.verify(userService, Mockito.never()).resendRegistrationOtp(
                Mockito.eq("attacker@example.com"), Mockito.anyString());
    }

    @Test
    void resendWithoutSessionContextIsRejectedWithoutUserLookup() throws Exception {
        mockMvc.perform(post("/resend-otp").param("email", "victim@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-otp"))
                .andExpect(model().attributeExists("errorMessage"));

        Mockito.verifyNoInteractions(userService);
    }

    @Test
    void successfulVerificationUsesSessionEmailAndClearsContext() throws Exception {
        MockHttpSession session = pendingSession("registered@example.com");

        mockMvc.perform(post("/verify-otp")
                        .session(session)
                        .param("email", "attacker@example.com")
                        .param("otpCode", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?verified=true"))
                .andExpect(request().sessionAttributeDoesNotExist("pendingRegistrationEmail"));

        Mockito.verify(userService).verifyRegistrationOtp("registered@example.com", "123456");
    }

    private MockHttpSession pendingSession(String email) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("pendingRegistrationEmail", email);
        return session;
    }
}
