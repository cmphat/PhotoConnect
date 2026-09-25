package com.photoconnect.security;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authoritative HTTP authentication boundary. A legacy session is populated only
 * after the cookie JWT and current database account have both been validated.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;
    private final JwtCookieService cookieService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   JwtCookieService cookieService,
                                   UserRepository userRepository) {
        this.tokenService = tokenService;
        this.cookieService = cookieService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = applicationPath(request);
        Optional<String> token = cookieService.read(request);
        AuthenticatedUser authenticatedUser = null;

        if (token.isPresent()) {
            try {
                JwtPrincipal principal = tokenService.parseToken(token.get());
                User user = userRepository.findById(principal.userId())
                        .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                        .filter(candidate -> candidate.getRole() == principal.role())
                        .orElseThrow(JwtValidationException::new);
                authenticatedUser = new AuthenticatedUser(user.getId(), user.getRole(), user.getFullName());
                if (hasConflictingSessionIdentity(request.getSession(false), authenticatedUser)) {
                    clearAuthentication(request, response, true);
                    reject(request, response, path, HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                request.setAttribute(CurrentUserContext.REQUEST_ATTRIBUTE, authenticatedUser);
                mirrorValidatedIdentity(request.getSession(true), user);
                response.setHeader("Cache-Control", "no-store");
            } catch (JwtValidationException ex) {
                clearAuthentication(request, response, true);
            }
        } else {
            clearAuthentication(request, response, false);
        }

        UserRole requiredRole = requiredRole(path);
        boolean authenticationRequired = requiredRole != null || requiresAnyAuthenticatedUser(path);
        if (authenticationRequired && authenticatedUser == null) {
            reject(request, response, path, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (requiredRole != null && authenticatedUser.role() != requiredRole) {
            reject(request, response, path, HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void mirrorValidatedIdentity(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userFullName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
    }

    private boolean hasConflictingSessionIdentity(HttpSession session, AuthenticatedUser user) {
        if (session == null) {
            return false;
        }
        Object sessionId = session.getAttribute("userId");
        Object sessionRole = session.getAttribute("userRole");
        if (sessionId == null && sessionRole == null) {
            return false;
        }
        return !(sessionId instanceof Long id) || !user.userId().equals(sessionId)
                || sessionRole == null || !user.role().name().equals(sessionRole.toString());
    }

    private void clearAuthentication(HttpServletRequest request, HttpServletResponse response, boolean clearCookie) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("userId");
            session.removeAttribute("userEmail");
            session.removeAttribute("userFullName");
            session.removeAttribute("userRole");
        }
        request.removeAttribute(CurrentUserContext.REQUEST_ATTRIBUTE);
        if (clearCookie) {
            cookieService.clear(request, response);
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String path, int status)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");
        if (isNonPageRequest(request, path)) {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getWriter().write(status == HttpServletResponse.SC_UNAUTHORIZED
                    ? "{\"error\":\"Authentication required\"}"
                    : "{\"error\":\"Access denied\"}");
        } else if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            response.sendError(status, "Access denied");
        }
    }

    private boolean isNonPageRequest(HttpServletRequest request, String path) {
        String accept = request.getHeader("Accept");
        return path.startsWith("/api/") || path.startsWith("/ws")
                || (accept != null && accept.contains("application/json"));
    }

    public static String applicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context != null && !context.isBlank() && uri.startsWith(context)
                ? uri.substring(context.length()) : uri;
    }

    static UserRole requiredRole(String path) {
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            return UserRole.ADMIN;
        }
        if (path.equals("/customer") || path.startsWith("/customer/")) {
            return UserRole.CUSTOMER;
        }
        if (path.equals("/photographer") || path.startsWith("/photographer/")) {
            return UserRole.PHOTOGRAPHER;
        }
        if (path.equals("/become-photographer")) {
            return UserRole.CUSTOMER;
        }
        if (path.matches("^/photographers/[^/]+/(book|save|unsave)$")) {
            return UserRole.CUSTOMER;
        }
        return null;
    }

    static boolean requiresAnyAuthenticatedUser(String path) {
        return path.equals("/auth/google/link")
                || path.equals("/bookings") || path.startsWith("/bookings/")
                || path.equals("/api/bookings") || path.startsWith("/api/bookings/")
                || path.equals("/ws") || path.startsWith("/ws/")
                || path.equals("/ws-raw") || path.startsWith("/ws-raw/");
    }
}
