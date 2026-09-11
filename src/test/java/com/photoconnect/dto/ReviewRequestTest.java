package com.photoconnect.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_withComment_shouldHaveNoViolations() {
        ReviewRequest request = new ReviewRequest(5, "Outstanding photography session!");

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void validRequest_withoutComment_shouldHaveNoViolations() {
        ReviewRequest request = new ReviewRequest(4, null);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void nullRating_shouldHaveViolation() {
        ReviewRequest request = new ReviewRequest(null, "Nice shoot.");

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Test
    void ratingLessThanOne_shouldHaveViolation() {
        ReviewRequest request = new ReviewRequest(0, "Poor.");

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Test
    void ratingGreaterThanFive_shouldHaveViolation() {
        ReviewRequest request = new ReviewRequest(6, "Super awesome!");

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Test
    void overlongComment_shouldHaveViolation() {
        String longComment = "A".repeat(1001);
        ReviewRequest request = new ReviewRequest(5, longComment);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("comment"));
    }

    @Test
    void exactlyMaxComment_shouldHaveNoViolations() {
        String exactComment = "A".repeat(1000);
        ReviewRequest request = new ReviewRequest(5, exactComment);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
