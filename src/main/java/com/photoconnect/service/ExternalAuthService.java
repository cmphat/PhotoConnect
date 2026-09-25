package com.photoconnect.service;

import com.photoconnect.entity.User;
import com.photoconnect.oauth.VerifiedExternalIdentity;

import java.util.Optional;

public interface ExternalAuthService {
    Optional<User> findLinkedUser(VerifiedExternalIdentity identity);
    User createCustomer(VerifiedExternalIdentity identity);
    User linkIdentity(Long userId, VerifiedExternalIdentity identity);
}
