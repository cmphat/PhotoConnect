package com.photoconnect.security;

import com.photoconnect.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class DefaultJwtCookieService implements JwtCookieService {

    private final JwtTokenService tokenService;
    private final boolean secure;

    public DefaultJwtCookieService(JwtTokenService tokenService, boolean secure) {
        this.tokenService = tokenService;
        this.secure = secure;
    }

    @Override
    public void issue(HttpServletRequest request, HttpServletResponse response, User user) {
        addCookie(response, request, tokenService.createToken(user), tokenService.getLifetime());
    }

    @Override
    public void clear(HttpServletRequest request, HttpServletResponse response) {
        addCookie(response, request, "", Duration.ZERO);
    }

    @Override
    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, HttpServletRequest request, String value, Duration maxAge) {
        String contextPath = request.getContextPath();
        String path = contextPath == null || contextPath.isBlank() ? "/" : contextPath;
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public boolean isSecure() {
        return secure;
    }
}
