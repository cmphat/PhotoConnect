package com.photoconnect.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoogleOAuth2FailureHandlerTest {

    private final GoogleOAuth2FailureHandler handler = new GoogleOAuth2FailureHandler();

    @Test
    void accessDeniedIsReportedAsCancellationWithoutLeakingProviderDetails() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationException(new OAuth2Error("access_denied")));
        assertEquals("/login?oauth=cancelled", response.getRedirectedUrl());
    }

    @Test
    void callbackFailureUsesGenericSafeError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationException(new OAuth2Error("invalid_id_token", "secret detail", null)));
        assertEquals("/login?oauth=error", response.getRedirectedUrl());
    }
}
