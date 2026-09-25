package com.photoconnect.security;

import com.photoconnect.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

public interface JwtCookieService {

    String COOKIE_NAME = "PHOTOCONNECT_AUTH";

    void issue(HttpServletRequest request, HttpServletResponse response, User user);
    void clear(HttpServletRequest request, HttpServletResponse response);
    Optional<String> read(HttpServletRequest request);
    boolean isSecure();
}
