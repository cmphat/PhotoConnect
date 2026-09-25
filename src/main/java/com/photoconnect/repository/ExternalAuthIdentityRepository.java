package com.photoconnect.repository;

import com.photoconnect.entity.ExternalAuthIdentity;
import com.photoconnect.entity.ExternalAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalAuthIdentityRepository extends JpaRepository<ExternalAuthIdentity, Long> {
    Optional<ExternalAuthIdentity> findByProviderAndProviderSubject(
            ExternalAuthProvider provider, String providerSubject);

    boolean existsByUserIdAndProvider(Long userId, ExternalAuthProvider provider);
}
