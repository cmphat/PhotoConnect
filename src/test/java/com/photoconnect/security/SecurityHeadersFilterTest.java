package com.photoconnect.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityHeadersFilter Unit Tests")
class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    @Test
    @DisplayName("Applies all defense-in-depth security response headers")
    void doFilterInternal_appliesAllExpectedSecurityHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/photographers");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Content-Type-Options"))
                .as("X-Content-Type-Options must be nosniff")
                .isEqualTo("nosniff");

        assertThat(response.getHeader("X-Frame-Options"))
                .as("X-Frame-Options must be SAMEORIGIN")
                .isEqualTo("SAMEORIGIN");

        assertThat(response.getHeader("X-XSS-Protection"))
                .as("X-XSS-Protection must be 0")
                .isEqualTo("0");

        assertThat(response.getHeader("Referrer-Policy"))
                .as("Referrer-Policy must be strict-origin-when-cross-origin")
                .isEqualTo("strict-origin-when-cross-origin");

        assertThat(response.getHeader("Permissions-Policy"))
                .as("Permissions-Policy must restrict sensitive features")
                .isEqualTo("camera=(), microphone=(), geolocation=()");
    }
}
