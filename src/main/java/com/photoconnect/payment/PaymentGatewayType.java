package com.photoconnect.payment;

public enum PaymentGatewayType {
    DEMO("Mô phỏng (Sandbox)"),
    PAYOS("PayOS VietQR Gateway"),
    SEPAY("SePay Bank Gateway"),
    STATIC_VIETQR("VietQR Direct Transfer");

    private final String displayName;

    PaymentGatewayType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
