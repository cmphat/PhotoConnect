package com.photoconnect.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GoogleOAuthUiAdvice {

    private final boolean configured;

    public GoogleOAuthUiAdvice(
            @Value("${photoconnect.oauth.google.client-id:}") String clientId,
            @Value("${photoconnect.oauth.google.client-secret:}") String clientSecret) {
        this.configured = clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    @ModelAttribute("googleOAuthEnabled")
    public boolean googleOAuthEnabled() {
        return configured;
    }
}
