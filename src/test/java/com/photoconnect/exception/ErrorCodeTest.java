package com.photoconnect.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    @DisplayName("Should have exactly 39 standardized error codes defined in docs/ERROR_CODES.md")
    void shouldHaveExpectedNumberOfErrorCodes() {
        ErrorCode[] codes = ErrorCode.values();
        assertThat(codes).hasSize(39);
    }

    @Test
    @DisplayName("All error codes should have non-null status, non-blank default message, and proper naming format")
    void allCodesShouldHaveValidMetadata() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(code.getHttpStatus()).isNotNull();
            assertThat(code.getDefaultMessage()).isNotBlank();
            assertThat(code.getCode()).isEqualTo(code.name());
            assertThat(code.name()).matches("^[A-Z]+_[0-9]{3}_[A-Z0-9_]+$");
        }
    }

    @Test
    @DisplayName("Key domain error codes should map to appropriate HTTP status codes")
    void keyCodesShouldMapToExpectedStatuses() {
        assertThat(ErrorCode.AUTH_001_INVALID_CREDENTIALS.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.AUTH_002_EMAIL_ALREADY_EXISTS.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.AUTH_005_ACCESS_DENIED.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(ErrorCode.USER_001_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.USER_002_ACCOUNT_LOCKED.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(ErrorCode.BOOKING_001_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.BOOKING_002_TIME_CONFLICT.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.BOOKING_007_SELF_BOOKING.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(ErrorCode.REVIEW_002_ALREADY_EXISTS.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.CHAT_002_ACCESS_DENIED.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(ErrorCode.SYSTEM_001_DATABASE_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorCode.SYSTEM_002_INTERNAL_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorCode.SYSTEM_003_VALIDATION_ERROR.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
