package com.photoconnect.oauth;

import java.io.Serializable;
import java.time.Instant;

public record OAuthFlowState(String state, String codeVerifier, Instant expiresAt,
                             Purpose purpose, Long linkingUserId) implements Serializable {
    public enum Purpose { LOGIN, LINK }
}
