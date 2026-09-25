package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.oauth.ExternalAccountLinkRequiredException;
import com.photoconnect.oauth.ExternalIdentityConflictException;
import com.photoconnect.oauth.VerifiedExternalIdentity;
import com.photoconnect.repository.ExternalAuthIdentityRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExternalAuthServiceTest {
    private ExternalAuthIdentityRepository identities;
    private UserRepository users;
    private ExternalAuthServiceImpl service;
    private final VerifiedExternalIdentity google = new VerifiedExternalIdentity(
            ExternalAuthProvider.GOOGLE, "google-subject-1", "person@example.com", "Person Name");

    @BeforeEach
    void setUp() {
        identities = mock(ExternalAuthIdentityRepository.class);
        users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("bcrypt-random-unavailable-password");
        service = new ExternalAuthServiceImpl(identities, users, encoder);
    }

    @Test
    void linkedIdentityReturnsActiveLocalUserWithPersistedRole() {
        User user = user(5L, UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
        ExternalAuthIdentity mapping = mapping(user, google.subject());
        when(identities.findByProviderAndProviderSubject(ExternalAuthProvider.GOOGLE, google.subject()))
                .thenReturn(Optional.of(mapping));
        assertSame(user, service.findLinkedUser(google).orElseThrow());
        assertEquals(UserRole.PHOTOGRAPHER, user.getRole());
    }

    @Test
    void inactiveLinkedAccountIsRejected() {
        User user = user(5L, UserRole.CUSTOMER, UserStatus.INACTIVE);
        when(identities.findByProviderAndProviderSubject(any(), any()))
                .thenReturn(Optional.of(mapping(user, google.subject())));
        assertThrows(AccountDisabledException.class, () -> service.findLinkedUser(google));
    }

    @Test
    void duplicateEmailCannotAutomaticallyTakeOverExistingAccount() {
        when(users.existsByEmail(google.email())).thenReturn(true);
        assertThrows(ExternalAccountLinkRequiredException.class, () -> service.findLinkedUser(google));
        assertThrows(ExternalAccountLinkRequiredException.class, () -> service.createCustomer(google));
        verify(users, never()).save(any());
        verify(identities, never()).saveAndFlush(any());
    }

    @Test
    void newExternalUserIsAlwaysCreatedAsActiveCustomerNeverAdmin() {
        when(users.existsByEmail(google.email())).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0); user.setId(20L); return user;
        });
        User created = service.createCustomer(google);
        assertEquals(UserRole.CUSTOMER, created.getRole());
        assertEquals(UserStatus.ACTIVE, created.getStatus());
        verify(identities).saveAndFlush(argThat(mapping -> mapping.getUser() == created
                && mapping.getProvider() == ExternalAuthProvider.GOOGLE
                && mapping.getProviderSubject().equals(google.subject())));
    }

    @Test
    void explicitLinkRejectsProviderSubjectOwnedByDifferentUser() {
        User current = user(3L, UserRole.CUSTOMER, UserStatus.ACTIVE);
        User other = user(4L, UserRole.ADMIN, UserStatus.ACTIVE);
        when(users.findById(3L)).thenReturn(Optional.of(current));
        when(identities.findByProviderAndProviderSubject(any(), any()))
                .thenReturn(Optional.of(mapping(other, google.subject())));
        assertThrows(ExternalIdentityConflictException.class, () -> service.linkIdentity(3L, google));
    }

    private User user(Long id, UserRole role, UserStatus status) {
        User user = new User(); user.setId(id); user.setRole(role); user.setStatus(status);
        user.setEmail("user" + id + "@example.com"); user.setFullName("User " + id); return user;
    }

    private ExternalAuthIdentity mapping(User user, String subject) {
        ExternalAuthIdentity mapping = new ExternalAuthIdentity(); mapping.setUser(user);
        mapping.setProvider(ExternalAuthProvider.GOOGLE); mapping.setProviderSubject(subject); return mapping;
    }
}
