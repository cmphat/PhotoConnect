package com.photoconnect.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

/**
 * PhotoConnect uses Google only to verify identity. Provider access and refresh
 * tokens are deliberately not retained after the login callback.
 */
public final class TransientOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
            String clientRegistrationId, Authentication principal, HttpServletRequest request) {
        return null;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
                                     Authentication principal,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        // Identity has already been validated by Spring Security OIDC. Do not persist provider tokens.
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId,
                                       Authentication principal,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        // Nothing is persisted.
    }
}
