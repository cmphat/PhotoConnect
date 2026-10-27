package com.photoconnect.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoogleOAuthSecurityConfigTest {

    @Test
    void nativeGoogleRegistrationUsesOidcAndStandardSpringCallback() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId("client.apps.googleusercontent.com");
        properties.setClientSecret("secret");

        ClientRegistrationRepository repository =
                new GoogleOAuthSecurityConfig().googleClientRegistrationRepository(properties);
        ClientRegistration google = repository.findByRegistrationId("google");

        assertEquals("https://accounts.google.com", google.getProviderDetails().getIssuerUri());
        assertEquals("https://accounts.google.com/o/oauth2/v2/auth",
                google.getProviderDetails().getAuthorizationUri());
        assertEquals("{baseUrl}/login/oauth2/code/{registrationId}", google.getRedirectUri());
        assertEquals("sub", google.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
        assertEquals(java.util.Set.of("openid", "profile", "email"), google.getScopes());
    }
}
