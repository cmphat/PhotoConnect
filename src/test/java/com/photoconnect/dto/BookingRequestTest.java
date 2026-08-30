package com.photoconnect.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_shouldHaveNoViolations() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDate.now().plusDays(3),
                LocalTime.of(14, 0),
                "Studio 5, District 1, Ho Chi Minh City",
                "Outdoor session preferred."
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void pastDate_shouldHaveViolation() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Somewhere",
                null
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bookingDate"));
    }

    @Test
    void nullDate_shouldHaveViolation() {
        BookingRequest request = new BookingRequest(
                1L,
                null,
                LocalTime.of(10, 0),
                "Somewhere",
                null
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bookingDate"));
    }

    @Test
    void nullTime_shouldHaveViolation() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDate.now().plusDays(1),
                null,
                "Somewhere",
                null
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bookingTime"));
    }

    @Test
    void blankLocation_shouldHaveViolation() {
        BookingRequest request = new BookingRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "   ",
                null
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("location"));
    }

    @Test
    void oversizedLocation_shouldHaveViolation() {
        String longLocation = "A".repeat(256);
        BookingRequest request = new BookingRequest(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                longLocation,
                null
        );

        Set<ConstraintViolation<BookingRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("location"));
    }
}
