package com.photoconnect.entity;

/** Payment choices supported by the local PhotoConnect demo environment. */
public enum DemoPaymentMethod {
    DEMO_QR("Demo QR / Bank Transfer"),
    DEMO_CARD("Demo Card");

    private final String displayName;

    DemoPaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DemoPaymentMethod fromFormValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Choose a demo payment method.");
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported demo payment method.");
        }
    }
}
