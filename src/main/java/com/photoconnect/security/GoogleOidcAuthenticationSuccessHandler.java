package com.photoconnect.security;

import com.photoconnect.config.GoogleOAuthProperties;
import com.photoconnect.entity.ExternalAuthProvider;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.oauth.ExternalAccountLinkRequiredException;
import com.photoconnect.oauth.ExternalIdentityConflictException;
import com.photoconnect.oauth.ExternalProviderException;
import com.photoconnect.oauth.VerifiedExternalIdentity;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.service.ExternalAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public final class GoogleOidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

    private final GoogleOAuthProperties properties;
    private final ExternalAuthService externalAuthService;
    private final JwtCookieService jwtCookieService;
    private final PhotographerProfileRepository photographerProfileRepository;

    public GoogleOidcAuthenticationSuccessHandler(GoogleOAuthProperties properties,
                                                   ExternalAuthService externalAuthService,
                                                   JwtCookieService jwtCookieService,
                                                   PhotographerProfileRepository photographerProfileRepository) {
        this.properties = properties;
        this.externalAuthService = externalAuthService;
        this.jwtCookieService = jwtCookieService;
        this.photographerProfileRepository = photographerProfileRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            VerifiedExternalIdentity identity = verifiedIdentity(authentication);
            Optional<User> linked = externalAuthService.findLinkedUser(identity);
            User user = linked.orElseGet(() -> externalAuthService.createCustomer(identity));
            establishApplicationLogin(request, response, user);
            clearSpringAuthentication(request);
            response.sendRedirect(request.getContextPath() + destination(user));
        } catch (ExternalAccountLinkRequiredException ex) {
            reject(request, response, "unlinked-email");
        } catch (AccountDisabledException ex) {
            reject(request, response, "account-disabled");
        } catch (ExternalIdentityConflictException ex) {
            reject(request, response, "identity-conflict");
        } catch (ExternalProviderException ex) {
            reject(request, response, "invalid-identity");
        }
    }

    private VerifiedExternalIdentity verifiedIdentity(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken token)
                || !"google".equals(token.getAuthorizedClientRegistrationId())
                || !(token.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new ExternalProviderException("Google did not return a valid OpenID Connect identity.");
        }
        if (oidcUser.getIssuer() == null
                || !GOOGLE_ISSUER.equals(oidcUser.getIssuer().toString())
                || oidcUser.getAudience() == null
                || !oidcUser.getAudience().contains(properties.getClientId())
                || oidcUser.getExpiresAt() == null
                || !oidcUser.getExpiresAt().isAfter(Instant.now())) {
            throw new ExternalProviderException("Google identity claims are invalid.");
        }
        if (!Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new ExternalProviderException("Google has not verified this email address.");
        }
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new ExternalProviderException("Google identity is incomplete.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String name = oidcUser.getFullName();
        if (name == null || name.isBlank()) {
            int at = normalizedEmail.indexOf('@');
            name = at > 0 ? normalizedEmail.substring(0, at) : "Google User";
        }
        return new VerifiedExternalIdentity(ExternalAuthProvider.GOOGLE,
                subject.trim(), normalizedEmail, name.trim());
    }

    private void establishApplicationLogin(HttpServletRequest request, HttpServletResponse response, User user) {
        request.changeSessionId();
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userFullName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
        jwtCookieService.issue(request, response, user);
    }

    private void clearSpringAuthentication(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        request.getSession().removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    }

    private String destination(User user) {
        if (user.getRole() == UserRole.ADMIN) return "/admin/dashboard";
        if (user.getRole() == UserRole.MANAGER) return "/manager/dashboard";
        if (user.getRole() == UserRole.PHOTOGRAPHER) {
            return photographerProfileRepository.findByUserId(user.getId())
                    .filter(profile -> profile.getVerificationStatus() == PhotographerVerificationStatus.APPROVED)
                    .map(profile -> "/photographer/dashboard")
                    .orElse("/photographer/onboarding-status");
        }
        return "/customer/dashboard";
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String code) throws IOException {
        clearSpringAuthentication(request);
        response.sendRedirect(request.getContextPath() + "/login?oauth=" + code);
    }
}
