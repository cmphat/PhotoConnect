package com.photoconnect.controller;

import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.oauth.*;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.security.AuthenticatedUser;
import com.photoconnect.security.CurrentUserContext;
import com.photoconnect.security.JwtCookieService;
import com.photoconnect.service.ExternalAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Controller
public class GoogleAuthController {

    static final String FLOW_SESSION_KEY = GoogleAuthController.class.getName() + ".flow";
    static final String PENDING_IDENTITY_KEY = GoogleAuthController.class.getName() + ".pendingIdentity";
    public static final String LINK_EMAIL_KEY = GoogleAuthController.class.getName() + ".linkEmail";
    private static final Duration FLOW_LIFETIME = Duration.ofMinutes(10);

    private final GoogleAuthClient googleAuthClient;
    private final ExternalAuthService externalAuthService;
    private final JwtCookieService jwtCookieService;
    private final CurrentUserContext currentUserContext;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleAuthController(GoogleAuthClient googleAuthClient,
                                ExternalAuthService externalAuthService,
                                JwtCookieService jwtCookieService,
                                CurrentUserContext currentUserContext,
                                PhotographerProfileRepository photographerProfileRepository) {
        this.googleAuthClient = googleAuthClient;
        this.externalAuthService = externalAuthService;
        this.jwtCookieService = jwtCookieService;
        this.currentUserContext = currentUserContext;
        this.photographerProfileRepository = photographerProfileRepository;
    }

    @GetMapping("/auth/google")
    public String startLogin(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (currentUserContext.current(request).isPresent()) return "redirect:/";
        return start(request.getSession(true), OAuthFlowState.Purpose.LOGIN, null, redirectAttributes);
    }

    @GetMapping("/auth/google/link")
    public String startLink(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Optional<AuthenticatedUser> current = currentUserContext.current(request);
        if (current.isEmpty()) return "redirect:/login";
        return start(request.getSession(true), OAuthFlowState.Purpose.LINK,
                current.get().userId(), redirectAttributes);
    }

    @GetMapping("/auth/google/callback")
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String state,
                           @RequestParam(required = false) String error,
                           HttpServletRequest request, HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        OAuthFlowState flow = session == null ? null : (OAuthFlowState) session.getAttribute(FLOW_SESSION_KEY);
        if (session != null) session.removeAttribute(FLOW_SESSION_KEY);
        if (!validFlow(flow, state)) return oauthError(redirectAttributes, "The Google sign-in request is invalid or expired.");
        if (error != null || code == null || code.isBlank()) {
            return oauthError(redirectAttributes, "Google sign-in was cancelled or could not be completed.");
        }
        try {
            VerifiedExternalIdentity identity = googleAuthClient.exchangeAndVerify(code, flow.codeVerifier());
            if (flow.purpose() == OAuthFlowState.Purpose.LINK) {
                AuthenticatedUser current = currentUserContext.current(request).orElse(null);
                if (current == null || !current.userId().equals(flow.linkingUserId())) {
                    return oauthError(redirectAttributes, "Your account-linking session is no longer valid.");
                }
                externalAuthService.linkIdentity(current.userId(), identity);
                redirectAttributes.addFlashAttribute("globalSuccess", "Google was linked to your account.");
                return "redirect:/";
            }

            Optional<User> linked = externalAuthService.findLinkedUser(identity);
            if (linked.isPresent()) {
                establishApplicationLogin(request, response, linked.get());
                return "redirect:" + destination(linked.get());
            }
            session.setAttribute(PENDING_IDENTITY_KEY, identity);
            session.setAttribute(PENDING_IDENTITY_KEY + ".expires", Instant.now().plus(FLOW_LIFETIME));
            return "redirect:/auth/google/onboarding";
        } catch (ExternalAccountLinkRequiredException ex) {
            session.setAttribute(LINK_EMAIL_KEY, "pending");
            redirectAttributes.addFlashAttribute("authError", ex.getMessage());
            return "redirect:/login?oauth=link-required";
        } catch (AccountDisabledException ex) {
            return oauthError(redirectAttributes, "Your PhotoConnect account is inactive. Contact support.");
        } catch (ExternalIdentityConflictException | ExternalProviderException ex) {
            return oauthError(redirectAttributes, ex.getMessage());
        }
    }

    @GetMapping("/auth/google/onboarding")
    public String showOnboarding(HttpSession session, Model model) {
        VerifiedExternalIdentity identity = pendingIdentity(session);
        if (identity == null) return "redirect:/login?oauth=expired";
        model.addAttribute("externalEmail", identity.email());
        model.addAttribute("externalName", identity.fullName());
        return "google-onboarding";
    }

    @PostMapping("/auth/google/onboarding")
    public String finishOnboarding(@RequestParam String role,
                                   HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        VerifiedExternalIdentity identity = pendingIdentity(session);
        if (identity == null) return oauthError(redirectAttributes, "Your Google registration expired. Please start again.");
        if (!"CUSTOMER".equals(role) && !"PHOTOGRAPHER".equals(role)) {
            redirectAttributes.addFlashAttribute("onboardingError", "Choose a valid account type.");
            return "redirect:/auth/google/onboarding";
        }
        try {
            User user = externalAuthService.createCustomer(identity);
            session.removeAttribute(PENDING_IDENTITY_KEY);
            session.removeAttribute(PENDING_IDENTITY_KEY + ".expires");
            establishApplicationLogin(request, response, user);
            return "PHOTOGRAPHER".equals(role) ? "redirect:/become-photographer" : "redirect:/customer/dashboard";
        } catch (ExternalAccountLinkRequiredException ex) {
            session.removeAttribute(PENDING_IDENTITY_KEY);
            redirectAttributes.addFlashAttribute("authError", ex.getMessage());
            return "redirect:/login?oauth=link-required";
        }
    }

    private String start(HttpSession session, OAuthFlowState.Purpose purpose, Long userId,
                         RedirectAttributes redirectAttributes) {
        try {
            String state = randomUrlToken(32);
            String verifier = randomUrlToken(64);
            String challenge = sha256Url(verifier);
            session.setAttribute(FLOW_SESSION_KEY,
                    new OAuthFlowState(state, verifier, Instant.now().plus(FLOW_LIFETIME), purpose, userId));
            return "redirect:" + googleAuthClient.buildAuthorizationUrl(challenge, state);
        } catch (ExternalProviderException ex) {
            return oauthError(redirectAttributes, ex.getMessage());
        }
    }

    private boolean validFlow(OAuthFlowState flow, String returnedState) {
        if (flow == null || returnedState == null || Instant.now().isAfter(flow.expiresAt())) return false;
        return MessageDigest.isEqual(flow.state().getBytes(StandardCharsets.UTF_8),
                returnedState.getBytes(StandardCharsets.UTF_8));
    }

    private VerifiedExternalIdentity pendingIdentity(HttpSession session) {
        Object expires = session.getAttribute(PENDING_IDENTITY_KEY + ".expires");
        Object identity = session.getAttribute(PENDING_IDENTITY_KEY);
        if (!(expires instanceof Instant instant) || Instant.now().isAfter(instant)
                || !(identity instanceof VerifiedExternalIdentity verified)) {
            session.removeAttribute(PENDING_IDENTITY_KEY);
            session.removeAttribute(PENDING_IDENTITY_KEY + ".expires");
            return null;
        }
        return verified;
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

    private String destination(User user) {
        if (user.getRole() == UserRole.ADMIN) return "/admin/dashboard";
        if (user.getRole() == UserRole.PHOTOGRAPHER) {
            return photographerProfileRepository.findByUserId(user.getId())
                    .filter(profile -> profile.getVerificationStatus() == PhotographerVerificationStatus.APPROVED)
                    .map(profile -> "/photographer/dashboard")
                    .orElse("/photographer/onboarding-status");
        }
        return "/customer/dashboard";
    }

    private String oauthError(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute("authError", message);
        return "redirect:/login?oauth=error";
    }

    private String randomUrlToken(int bytes) {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String sha256Url(String value) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
