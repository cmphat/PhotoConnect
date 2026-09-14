package com.photoconnect.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class DummyController {

        @GetMapping("/api/test/domain-error")
        public void throwDomainError() {
            throw new InvalidBookingException("Booking does not exist");
        }

        @GetMapping("/page/test/domain-error")
        public void throwDomainErrorHtml() {
            throw new PhotoConnectException(ErrorCode.AUTH_005_ACCESS_DENIED, "Forbidden operation");
        }

        @PostMapping("/api/test/validation")
        public void validateDto(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/api/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid search bounds");
        }

        @GetMapping("/api/test/server-error")
        public void throwUnexpectedError() {
            throw new RuntimeException("Database timeout failure");
        }

        @GetMapping("/page/test/server-error")
        public void throwUnexpectedErrorHtml() {
            throw new NullPointerException("Null pointer simulation");
        }
    }

    static class TestRequest {
        @NotBlank(message = "Field name cannot be blank")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("API request encountering PhotoConnectException should return structured JSON with specific ErrorCode")
    void apiDomainExceptionReturnsStandardJson() throws Exception {
        mockMvc.perform(get("/api/test/domain-error"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_001_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Booking does not exist"));
    }

    @Test
    @DisplayName("HTML request encountering PhotoConnectException should render error.jsp with model attributes")
    void htmlDomainExceptionRendersErrorView() throws Exception {
        mockMvc.perform(get("/page/test/domain-error").accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 403))
                .andExpect(model().attribute("errorCode", "AUTH_005_ACCESS_DENIED"))
                .andExpect(model().attribute("errorMessage", "Forbidden operation"));
    }

    @Test
    @DisplayName("API request with validation failure should return 400 and SYSTEM_003_VALIDATION_ERROR with field map")
    void apiValidationFailureReturnsValidationErrorCode() throws Exception {
        mockMvc.perform(post("/api/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_003_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.name").value("Field name cannot be blank"));
    }

    @Test
    @DisplayName("API request with IllegalArgumentException should return 400 and SYSTEM_003_VALIDATION_ERROR")
    void apiIllegalArgumentReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_003_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid search bounds"));
    }

    @Test
    @DisplayName("API request encountering unexpected server exception should return 500 without leaking stack traces")
    void apiUnexpectedExceptionReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/api/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SYSTEM_002_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred. Please try again later."));
    }

    @Test
    @DisplayName("HTML request encountering unexpected server exception should render error.jsp with 500 status")
    void htmlUnexpectedExceptionRendersErrorViewWith500() throws Exception {
        mockMvc.perform(get("/page/test/server-error").accept(MediaType.TEXT_HTML))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 500))
                .andExpect(model().attribute("errorCode", "SYSTEM_002_INTERNAL_ERROR"));
    }
}
