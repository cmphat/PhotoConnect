package com.photoconnect;

import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "photoconnect.mail.capture-mode=true")
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_INTEGRATION_TESTS", matches = "true")
class RegistrationEndToEndIntegrationTest {

    private static final Pattern OTP_PATTERN = Pattern.compile("\\b(\\d{6})\\b");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void clearCapturedMail() {
        emailService.clearCapturedEmails();
    }

    @Test
    void registrationOtpActivationAndLoginCompleteWithCsrfProtection() throws Exception {
        String email = "registration-e2e-" + UUID.randomUUID() + "@example.invalid";
        String password = "E2e-Registration-2026!";

        MvcResult form = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) form.getRequest().getSession(false);
        CsrfToken csrf = (CsrfToken) form.getRequest().getAttribute(CsrfToken.class.getName());
        assertThat(session).isNotNull();
        assertThat(csrf).isNotNull();

        mockMvc.perform(post("/register")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("fullName", "Registration E2E")
                        .param("email", email)
                        .param("phone", "0901234567")
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify-otp?sent=true"));

        mockMvc.perform(get("/verify-otp").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/verify-otp").session(session))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(email)).get()
                .extracting("status")
                .isEqualTo(UserStatus.PENDING_VERIFICATION);

        EmailService.CapturedEmail verificationMail = emailService.getLastCapturedEmail(email);
        assertThat(verificationMail).isNotNull();
        Matcher otpMatcher = OTP_PATTERN.matcher(verificationMail.body());
        assertThat(otpMatcher.find()).isTrue();
        String otp = otpMatcher.group(1);

        mockMvc.perform(post("/verify-otp")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("otpCode", otp)
                        .param("email", "tampered@example.invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?verified=true"));

        assertThat(userRepository.findByEmail(email)).get()
                .extracting("status")
                .isEqualTo(UserStatus.ACTIVE);

        MvcResult login = mockMvc.perform(post("/login")
                        .session(session)
                        .param(csrf.getParameterName(), csrf.getToken())
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        HttpSession authenticatedSession = login.getRequest().getSession(false);
        assertThat(authenticatedSession).isNotNull();
        assertThat(authenticatedSession.getAttribute("userId")).isNotNull();
        assertThat(login.getResponse().getCookie("PHOTOCONNECT_AUTH")).isNotNull();
    }

    @Test
    void registrationWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Missing CSRF")
                        .param("email", "missing-csrf@example.invalid")
                        .param("password", "Missing-Csrf-2026!")
                        .param("confirmPassword", "Missing-Csrf-2026!"))
                .andExpect(status().isForbidden());
    }
}
