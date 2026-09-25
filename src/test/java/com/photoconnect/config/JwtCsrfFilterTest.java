package com.photoconnect.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;

import static org.junit.jupiter.api.Assertions.*;

class JwtCsrfFilterTest {

    @Test
    void unsafeMvcRequestNeedsTokenAndLegitimateFormTokenWorks() throws Exception {
        Filter filter = csrfFilter();
        MockHttpServletRequest pageRequest = new MockHttpServletRequest("GET", "/bookings/1");
        filter.doFilter(pageRequest, new MockHttpServletResponse(), new MockFilterChain());
        CsrfToken token = (CsrfToken) pageRequest.getAttribute("_csrf");
        assertNotNull(token);
        String tokenValue = token.getToken();

        MockHttpServletRequest rejected = new MockHttpServletRequest("POST", "/bookings/1/cancel");
        rejected.setSession(pageRequest.getSession());
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
        filter.doFilter(rejected, rejectedResponse, new MockFilterChain());
        assertEquals(403, rejectedResponse.getStatus());

        MockHttpServletRequest accepted = new MockHttpServletRequest("POST", "/bookings/1/cancel");
        accepted.setSession(pageRequest.getSession());
        accepted.addParameter(token.getParameterName(), tokenValue);
        MockFilterChain acceptedChain = new MockFilterChain();
        filter.doFilter(accepted, new MockHttpServletResponse(), acceptedChain);
        assertNotNull(acceptedChain.getRequest());
    }

    @Test
    void sockJsTransportIsExemptButStillHandledByJwtAndOriginBoundariesElsewhere() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ws/123/xhr_send");
        MockFilterChain chain = new MockFilterChain();

        csrfFilter().doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(chain.getRequest());
    }

    @Test
    void googleOnboardingPostRequiresCsrfToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/google/onboarding");
        MockHttpServletResponse response = new MockHttpServletResponse();
        csrfFilter().doFilter(request, response, new MockFilterChain());
        assertEquals(403, response.getStatus());
    }

    private Filter csrfFilter() {
        FilterRegistrationBean<CsrfFilter> registration = new JwtSecurityConfig().csrfFilterRegistration();
        return registration.getFilter();
    }
}
