package com.photoconnect.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleOAuthPropertiesTest {

    @Test
    void optionalCredentialsRequireBothValues() {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        assertFalse(properties.isConfigured());
        properties.setClientId("client-id");
        assertFalse(properties.isConfigured());
        properties.setClientSecret("client-secret");
        assertTrue(properties.isConfigured());
    }
}
