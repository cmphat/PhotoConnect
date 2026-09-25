package com.photoconnect.config;

import com.photoconnect.repository.UserRepository;
import com.photoconnect.security.JwtAuthenticationFilter;
import com.photoconnect.security.JwtCookieService;
import com.photoconnect.security.JwtTokenService;
import com.photoconnect.security.DefaultJwtCookieService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

@Configuration
public class JwtSecurityConfig {

    @Bean
    public JwtTokenService jwtTokenService(
            @Value("${photoconnect.jwt.secret:}") String secret,
            @Value("${photoconnect.jwt.expiration:PT30M}") Duration expiration) {
        return new JwtTokenService(secret, expiration);
    }

    @Bean
    public JwtCookieService jwtCookieService(
            JwtTokenService tokenService,
            @Value("${photoconnect.jwt.cookie-secure:false}") boolean cookieSecure) {
        return new DefaultJwtCookieService(tokenService, cookieSecure);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtTokenService tokenService, JwtCookieService cookieService, UserRepository userRepository) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(tokenService, cookieService, userRepository));
        registration.setName("jwtAuthenticationFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        CsrfFilter filter = new CsrfFilter(repository);
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        filter.setRequestHandler(requestHandler);
        Set<String> safeMethods = Set.of("GET", "HEAD", "TRACE", "OPTIONS");
        filter.setRequireCsrfProtectionMatcher(request -> {
            String path = JwtAuthenticationFilter.applicationPath(request);
            return !safeMethods.contains(request.getMethod())
                    && !path.equals("/ws") && !path.startsWith("/ws/")
                    && !path.equals("/ws-raw") && !path.startsWith("/ws-raw/");
        });
        filter.setAccessDeniedHandler((request, response, exception) -> {
            String path = JwtAuthenticationFilter.applicationPath(request);
            response.setHeader("Cache-Control", "no-store");
            if (path.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid CSRF token\"}");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            }
        });

        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setName("csrfFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.addUrlPatterns("/*");
        return registration;
    }
}
