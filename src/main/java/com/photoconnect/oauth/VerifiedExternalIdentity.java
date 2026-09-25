package com.photoconnect.oauth;

import com.photoconnect.entity.ExternalAuthProvider;

import java.io.Serializable;

public record VerifiedExternalIdentity(ExternalAuthProvider provider, String subject,
                                       String email, String fullName) implements Serializable {
}
