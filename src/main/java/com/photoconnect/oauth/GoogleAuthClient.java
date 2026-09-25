package com.photoconnect.oauth;

public interface GoogleAuthClient {
    String buildAuthorizationUrl(String codeChallenge, String state);
    VerifiedExternalIdentity exchangeAndVerify(String authorizationCode, String codeVerifier);
}
