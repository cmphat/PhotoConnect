package com.photoconnect.util;

import com.photoconnect.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class SessionSecurityUtilsTest {

    @Test
    void missingIdentityRedirectsToLogin() {
        assertThat(SessionSecurityUtils.requireRole(new MockHttpSession(), UserRole.CUSTOMER))
                .isEqualTo("redirect:/login");
    }

    @Test
    void wrongRoleRedirectsAwayFromProtectedArea() {
        MockHttpSession session = session(10L, "PHOTOGRAPHER");

        assertThat(SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER))
                .isEqualTo("redirect:/");
    }

    @Test
    void matchingRoleReturnsAuthenticatedUserId() {
        MockHttpSession session = session(10L, UserRole.CUSTOMER);

        assertThat(SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER)).isNull();
        assertThat(SessionSecurityUtils.userId(session)).isEqualTo(10L);
    }

    @Test
    void malformedSessionIdentityIsRejectedSafely() {
        MockHttpSession session = session("10", "CUSTOMER");

        assertThat(SessionSecurityUtils.userId(session)).isNull();
        assertThat(SessionSecurityUtils.requireRole(session, UserRole.CUSTOMER))
                .isEqualTo("redirect:/login");
    }

    private MockHttpSession session(Object userId, Object role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userRole", role);
        return session;
    }
}
