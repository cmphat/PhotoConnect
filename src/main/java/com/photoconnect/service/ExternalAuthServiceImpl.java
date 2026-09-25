package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.exception.AccountDisabledException;
import com.photoconnect.oauth.ExternalAccountLinkRequiredException;
import com.photoconnect.oauth.ExternalIdentityConflictException;
import com.photoconnect.oauth.ExternalProviderException;
import com.photoconnect.oauth.VerifiedExternalIdentity;
import com.photoconnect.repository.ExternalAuthIdentityRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class ExternalAuthServiceImpl implements ExternalAuthService {

    private final ExternalAuthIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ExternalAuthServiceImpl(ExternalAuthIdentityRepository identityRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findLinkedUser(VerifiedExternalIdentity identity) {
        validate(identity);
        Optional<User> linked = identityRepository.findByProviderAndProviderSubject(identity.provider(), identity.subject())
                .map(ExternalAuthIdentity::getUser)
                .map(this::requireActive);
        if (linked.isEmpty() && userRepository.existsByEmail(identity.email())) {
            throw new ExternalAccountLinkRequiredException();
        }
        return linked;
    }

    @Override
    @Transactional
    public User createCustomer(VerifiedExternalIdentity identity) {
        validate(identity);
        if (userRepository.existsByEmail(identity.email())) {
            throw new ExternalAccountLinkRequiredException();
        }
        User user = new User();
        user.setEmail(identity.email());
        user.setFullName(limit(identity.fullName(), 150));
        user.setPassword(passwordEncoder.encode(randomUnavailablePassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        persistIdentity(saved, identity);
        return saved;
    }

    @Override
    @Transactional
    public User linkIdentity(Long userId, VerifiedExternalIdentity identity) {
        validate(identity);
        User user = requireActive(userRepository.findById(userId)
                .orElseThrow(() -> new ExternalIdentityConflictException("The local account no longer exists.")));
        Optional<ExternalAuthIdentity> existingSubject = identityRepository
                .findByProviderAndProviderSubject(identity.provider(), identity.subject());
        if (existingSubject.isPresent()) {
            if (existingSubject.get().getUser().getId().equals(userId)) return user;
            throw new ExternalIdentityConflictException("This Google identity is already linked to another account.");
        }
        if (identityRepository.existsByUserIdAndProvider(userId, identity.provider())) {
            throw new ExternalIdentityConflictException("This account already has a Google identity linked.");
        }
        persistIdentity(user, identity);
        return user;
    }

    private void persistIdentity(User user, VerifiedExternalIdentity identity) {
        ExternalAuthIdentity mapping = new ExternalAuthIdentity();
        mapping.setUser(user);
        mapping.setProvider(identity.provider());
        mapping.setProviderSubject(identity.subject());
        try {
            identityRepository.saveAndFlush(mapping);
        } catch (DataIntegrityViolationException ex) {
            throw new ExternalIdentityConflictException("This Google identity is already linked.");
        }
    }

    private User requireActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountDisabledException("Your PhotoConnect account is not active.");
        }
        return user;
    }

    private void validate(VerifiedExternalIdentity identity) {
        if (identity == null || identity.provider() != ExternalAuthProvider.GOOGLE
                || identity.subject() == null || identity.subject().isBlank() || identity.subject().length() > 255
                || identity.email() == null || identity.email().isBlank()
                || identity.fullName() == null || identity.fullName().isBlank()) {
            throw new ExternalProviderException("The verified Google identity is incomplete.");
        }
    }

    private String randomUnavailablePassword() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String limit(String value, int length) {
        String trimmed = value.trim();
        return trimmed.length() <= length ? trimmed : trimmed.substring(0, length);
    }
}
