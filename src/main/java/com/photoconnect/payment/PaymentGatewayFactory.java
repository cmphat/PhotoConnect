package com.photoconnect.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentGatewayFactory {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayFactory.class);

    private final Map<PaymentGatewayType, PaymentGatewayAdapter> adapters = new ConcurrentHashMap<>();
    private final String paymentMode;
    private final boolean realEnabled;
    private final String preferredProvider;

    public PaymentGatewayFactory(
            List<PaymentGatewayAdapter> adapterList,
            @Value("${photoconnect.payment.mode:DEMO}") String paymentMode,
            @Value("${photoconnect.payment.real-enabled:false}") boolean realEnabled,
            @Value("${photoconnect.payment.provider:demo}") String preferredProvider) {
        for (PaymentGatewayAdapter adapter : adapterList) {
            adapters.put(adapter.getType(), adapter);
        }
        this.paymentMode = paymentMode != null ? paymentMode.trim().toUpperCase() : "DEMO";
        this.realEnabled = realEnabled;
        this.preferredProvider = preferredProvider != null ? preferredProvider.trim().toLowerCase() : "demo";
        log.info("PaymentGatewayFactory initialized: mode={}, realEnabled={}, preferredProvider={}",
                this.paymentMode, this.realEnabled, this.preferredProvider);
    }

    public PaymentGatewayAdapter getActiveAdapter() {
        if (!"REAL".equalsIgnoreCase(paymentMode) || !realEnabled) {
            return adapters.getOrDefault(PaymentGatewayType.DEMO, null);
        }

        if ("payos".equalsIgnoreCase(preferredProvider)) {
            PaymentGatewayAdapter payos = adapters.get(PaymentGatewayType.PAYOS);
            if (payos != null && payos.isAvailable()) {
                return payos;
            }
        } else if ("sepay".equalsIgnoreCase(preferredProvider)) {
            PaymentGatewayAdapter sepay = adapters.get(PaymentGatewayType.SEPAY);
            if (sepay != null && sepay.isAvailable()) {
                return sepay;
            }
        }

        log.error("Real payment mode was requested, but provider '{}' is unavailable or incomplete. Checkout is disabled.", preferredProvider);
        return null;
    }

    public PaymentGatewayAdapter getAdapter(PaymentGatewayType type) {
        return adapters.get(type);
    }

    public PaymentGatewayAdapter getAdapterByName(String providerName) {
        if (!isRealModeActive()) return null;
        if (providerName == null) return null;
        String normalized = providerName.trim().toUpperCase();
        for (PaymentGatewayAdapter adapter : adapters.values()) {
            if (adapter.getType().name().equalsIgnoreCase(normalized)) {
                return adapter;
            }
        }
        return null;
    }

    public boolean isRealModeActive() {
        if (!"REAL".equalsIgnoreCase(paymentMode) || !realEnabled) return false;
        PaymentGatewayAdapter adapter;
        if ("payos".equalsIgnoreCase(preferredProvider)) {
            adapter = adapters.get(PaymentGatewayType.PAYOS);
        } else if ("sepay".equalsIgnoreCase(preferredProvider)) {
            adapter = adapters.get(PaymentGatewayType.SEPAY);
        } else {
            return false;
        }
        return adapter != null && adapter.isAvailable();
    }

    public boolean isRealModeRequested() {
        return "REAL".equalsIgnoreCase(paymentMode);
    }
}
