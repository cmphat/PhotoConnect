package com.photoconnect.oauth;

public class ExternalAccountLinkRequiredException extends RuntimeException {
    public ExternalAccountLinkRequiredException() {
        super("An existing PhotoConnect account uses this email. Sign in with its password to link Google safely.");
    }
}
