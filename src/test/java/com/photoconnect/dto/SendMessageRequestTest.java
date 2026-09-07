package com.photoconnect.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SendMessageRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_shouldHaveNoViolations() {
        SendMessageRequest request = new SendMessageRequest(1L, "Hello, looking forward to the shoot!");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankContent_shouldHaveViolation() {
        SendMessageRequest request = new SendMessageRequest(1L, "   ");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void nullContent_shouldHaveViolation() {
        SendMessageRequest request = new SendMessageRequest(1L, null);

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void oversizedContent_shouldHaveViolation() {
        String longContent = "A".repeat(2001);
        SendMessageRequest request = new SendMessageRequest(1L, longContent);

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void exactlyMaxContent_shouldHaveNoViolations() {
        String exactContent = "A".repeat(2000);
        SendMessageRequest request = new SendMessageRequest(1L, exactContent);

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
