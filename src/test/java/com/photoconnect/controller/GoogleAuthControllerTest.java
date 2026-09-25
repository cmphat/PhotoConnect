package com.photoconnect.controller;

import com.photoconnect.entity.*;
import com.photoconnect.oauth.*;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.security.*;
import com.photoconnect.service.ExternalAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoogleAuthControllerTest {
    private GoogleAuthClient client;
    private ExternalAuthService externalAuth;
    private JwtCookieService cookies;
    private CurrentUserContext currentUser;
    private PhotographerProfileRepository profiles;
    private MockMvc mvc;
    private final VerifiedExternalIdentity identity = new VerifiedExternalIdentity(
            ExternalAuthProvider.GOOGLE, "subject", "google@example.com", "Google User");

    @BeforeEach
    void setUp() {
        client = mock(GoogleAuthClient.class); externalAuth = mock(ExternalAuthService.class);
        cookies = mock(JwtCookieService.class); currentUser = mock(CurrentUserContext.class);
        profiles = mock(PhotographerProfileRepository.class);
        when(currentUser.current(any())).thenReturn(Optional.empty());
        mvc = MockMvcBuilders.standaloneSetup(new GoogleAuthController(
                client, externalAuth, cookies, currentUser, profiles)).build();
    }

    @Test
    void initiationStoresFlowAndRedirectsToRealProviderUrl() throws Exception {
        when(client.buildAuthorizationUrl(anyString(), anyString())).thenReturn("https://project.supabase.co/auth/v1/authorize?provider=google");
        mvc.perform(get("/auth/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://project.supabase.co/auth/v1/authorize?provider=google"))
                .andExpect(request().sessionAttribute(GoogleAuthController.FLOW_SESSION_KEY,
                        org.hamcrest.Matchers.instanceOf(OAuthFlowState.class)));
    }

    @Test
    void invalidAndExpiredStateRejectCallbackWithoutExchange() throws Exception {
        MockHttpSession session = flowSession(OAuthFlowState.Purpose.LOGIN, null, "expected", Instant.now().minusSeconds(1));
        mvc.perform(get("/auth/google/callback").session(session).param("state", "wrong").param("code", "code"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?oauth=error"));
        verify(client, never()).exchangeAndVerify(anyString(), anyString());
    }

    @Test
    void linkedIdentityIssuesExistingApplicationJwtAndDoesNotCreateUser() throws Exception {
        User user = user(8L, UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(client.exchangeAndVerify("code", "verifier")).thenReturn(identity);
        when(externalAuth.findLinkedUser(identity)).thenReturn(Optional.of(user));
        MockHttpSession session = flowSession(OAuthFlowState.Purpose.LOGIN, null, "state", Instant.now().plusSeconds(60));
        mvc.perform(get("/auth/google/callback").session(session).param("state", "state").param("code", "code"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/customer/dashboard"))
                .andExpect(request().sessionAttribute("userId", 8L));
        verify(cookies).issue(any(), any(), same(user));
        verify(externalAuth, never()).createCustomer(any());
    }

    @Test
    void unlinkedIdentityMovesToPendingOnboarding() throws Exception {
        when(client.exchangeAndVerify(anyString(), anyString())).thenReturn(identity);
        when(externalAuth.findLinkedUser(identity)).thenReturn(Optional.empty());
        MockHttpSession session = flowSession(OAuthFlowState.Purpose.LOGIN, null, "state", Instant.now().plusSeconds(60));
        mvc.perform(get("/auth/google/callback").session(session).param("state", "state").param("code", "code"))
                .andExpect(redirectedUrl("/auth/google/onboarding"))
                .andExpect(request().sessionAttribute(GoogleAuthController.PENDING_IDENTITY_KEY, identity));
    }

    @Test
    void photographerChoiceCreatesCustomerThenUsesExistingApprovalOnboarding() throws Exception {
        User customer = user(9L, UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(externalAuth.createCustomer(identity)).thenReturn(customer);
        MockHttpSession session = pendingSession();
        mvc.perform(post("/auth/google/onboarding").session(session).param("role", "PHOTOGRAPHER"))
                .andExpect(redirectedUrl("/become-photographer"));
        verify(cookies).issue(any(), any(), same(customer));
    }

    @Test
    void adminRoleParameterCannotEscalatePrivileges() throws Exception {
        mvc.perform(post("/auth/google/onboarding").session(pendingSession()).param("role", "ADMIN"))
                .andExpect(redirectedUrl("/auth/google/onboarding"));
        verify(externalAuth, never()).createCustomer(any());
    }

    @Test
    void explicitLinkRequiresSameAuthenticatedUserWhoStartedFlow() throws Exception {
        when(client.exchangeAndVerify(anyString(), anyString())).thenReturn(identity);
        when(currentUser.current(any())).thenReturn(Optional.of(new AuthenticatedUser(11L, UserRole.CUSTOMER, "User")));
        MockHttpSession session = flowSession(OAuthFlowState.Purpose.LINK, 11L, "state", Instant.now().plusSeconds(60));
        mvc.perform(get("/auth/google/callback").session(session).param("state", "state").param("code", "code"))
                .andExpect(redirectedUrl("/"));
        verify(externalAuth).linkIdentity(11L, identity);
    }

    @Test
    void cancelledProviderFlowShowsSafeError() throws Exception {
        MockHttpSession session = flowSession(OAuthFlowState.Purpose.LOGIN, null, "state", Instant.now().plusSeconds(60));
        mvc.perform(get("/auth/google/callback").session(session).param("state", "state").param("error", "access_denied"))
                .andExpect(redirectedUrl("/login?oauth=error"))
                .andExpect(flash().attributeExists("authError"));
    }

    private MockHttpSession flowSession(OAuthFlowState.Purpose purpose, Long userId, String state, Instant expiry) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(GoogleAuthController.FLOW_SESSION_KEY,
                new OAuthFlowState(state, "verifier", expiry, purpose, userId));
        return session;
    }

    private MockHttpSession pendingSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(GoogleAuthController.PENDING_IDENTITY_KEY, identity);
        session.setAttribute(GoogleAuthController.PENDING_IDENTITY_KEY + ".expires", Instant.now().plusSeconds(60));
        return session;
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User(); user.setId(id); user.setEmail("user@example.com"); user.setFullName("User");
        user.setRole(role); user.setStatus(status); return user;
    }
}
