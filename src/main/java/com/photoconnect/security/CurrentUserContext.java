package com.photoconnect.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface CurrentUserContext {

    public static final String REQUEST_ATTRIBUTE = CurrentUserContext.class.getName() + ".authenticatedUser";

    Optional<AuthenticatedUser> current(HttpServletRequest request);
}
