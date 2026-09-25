package com.photoconnect.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestAttributeCurrentUserContext implements CurrentUserContext {
    @Override
    public Optional<AuthenticatedUser> current(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        return value instanceof AuthenticatedUser user ? Optional.of(user) : Optional.empty();
    }
}
