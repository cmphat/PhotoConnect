package com.photoconnect.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebSocketConfigTest {

    @Test
    void explicitOrigins_areAccepted() {
        assertDoesNotThrow(() -> new WebSocketConfig("http://localhost:8080, https://demo.example"));
    }

    @Test
    void wildcardOrEmptyOrigins_areRejected() {
        assertThrows(IllegalArgumentException.class, () -> new WebSocketConfig("*"));
        assertThrows(IllegalArgumentException.class, () -> new WebSocketConfig("  "));
    }
}
