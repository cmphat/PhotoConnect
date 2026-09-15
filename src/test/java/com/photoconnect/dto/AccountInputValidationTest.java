package com.photoconnect.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountInputValidationTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void registrationRejectsMalformedPhoneAndOversizedConfirmation() {
        RegisterRequest request = new RegisterRequest(
                "Valid Name", "valid@example.com", "call-me", "password123", "x".repeat(73));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("phone", "confirmPassword");
    }

    @Test
    void loginRejectsPasswordAboveBcryptLimit() {
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("x".repeat(73));

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("password"));
    }

    @Test
    void photographerPriceRejectsUnsupportedScale() {
        PhotographerProfileRequest request = new PhotographerProfileRequest();
        request.setDisplayName("Studio Name");
        request.setBio("A sufficiently long photographer biography.");
        request.setCity("Da Nang");
        request.setExperienceYears(5);
        request.setPriceFrom(new BigDecimal("1000.999"));

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("priceFrom"));
    }

    @Test
    void devSeedPasswordHashMatchesPassword123() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String devSeedHash = "$2a$10$MswCCZEoO3Q91enCgvZI.OWqz34HhwHAdFEnJ22YO02gJx07Siqqy";
        assertThat(encoder.matches("password123", devSeedHash)).isTrue();
    }
}
