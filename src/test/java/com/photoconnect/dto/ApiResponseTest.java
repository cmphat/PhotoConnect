package com.photoconnect.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoconnect.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("ApiResponse.success should construct valid success envelope")
    void successResponseContract() throws Exception {
        ApiResponse<String> response = ApiResponse.success("Hello World");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("Hello World");
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getErrors()).isNull();

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"data\":\"Hello World\"");
        assertThat(json).contains("\"message\":\"OK\"");
        assertThat(json).doesNotContain("errorCode");
    }

    @Test
    @DisplayName("ApiResponse.error should construct valid error envelope matching API_CONTRACT.md")
    void errorResponseContract() throws Exception {
        ApiResponse<?> response = ApiResponse.error(ErrorCode.BOOKING_002_TIME_CONFLICT, "Photographer is not available at this time");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getErrorCode()).isEqualTo("BOOKING_002_TIME_CONFLICT");
        assertThat(response.getMessage()).isEqualTo("Photographer is not available at this time");

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"success\":false");
        assertThat(json).contains("\"errorCode\":\"BOOKING_002_TIME_CONFLICT\"");
        assertThat(json).contains("\"message\":\"Photographer is not available at this time\"");
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    @DisplayName("ApiResponse.validationError should include structured field errors map")
    void validationErrorContract() throws Exception {
        Map<String, String> fieldErrors = Map.of(
                "email", "Please provide a valid email address",
                "password", "Password must be at least 8 characters"
        );

        ApiResponse<?> response = ApiResponse.validationError("Validation failed", fieldErrors);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("SYSTEM_003_VALIDATION_ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getErrors()).containsEntry("email", "Please provide a valid email address");

        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"success\":false");
        assertThat(json).contains("\"errorCode\":\"SYSTEM_003_VALIDATION_ERROR\"");
        assertThat(json).contains("\"errors\":{");
    }
}
