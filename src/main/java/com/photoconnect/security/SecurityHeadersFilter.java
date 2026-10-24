package com.photoconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Applies defense-in-depth HTTP response security headers across all incoming requests:
 * <ul>
 *     <li><b>X-Content-Type-Options: nosniff</b> - Blocks MIME type sniffing.</li>
 *     <li><b>X-Frame-Options: SAMEORIGIN</b> - Prevents clickjacking attacks while allowing valid same-origin framing.</li>
 *     <li><b>X-XSS-Protection: 0</b> - Disables deprecated buggy browser XSS filters in favor of CSP & modern browser defenses.</li>
 *     <li><b>Referrer-Policy: strict-origin-when-cross-origin</b> - Protects sensitive path and query parameters from leaking across origins.</li>
 *     <li><b>Permissions-Policy</b> - Restricts unused hardware capabilities (camera, microphone, geolocation).</li>
 * </ul>
 */
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "0");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        filterChain.doFilter(request, response);
    }
}
