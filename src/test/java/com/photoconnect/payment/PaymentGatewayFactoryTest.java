package com.photoconnect.payment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentGatewayFactoryTest {

    @Test
    void realModeNeverFallsBackToDemoWhenProviderUnavailable() {
        PaymentGatewayAdapter demo = mock(PaymentGatewayAdapter.class);
        when(demo.getType()).thenReturn(PaymentGatewayType.DEMO);
        PaymentGatewayAdapter payos = mock(PaymentGatewayAdapter.class);
        when(payos.getType()).thenReturn(PaymentGatewayType.PAYOS);
        when(payos.isAvailable()).thenReturn(false);
        PaymentGatewayFactory factory = new PaymentGatewayFactory(List.of(demo, payos), "REAL", true, "payos");

        assertThat(factory.getActiveAdapter()).isNull();
        assertThat(factory.isRealModeActive()).isFalse();
        assertThat(factory.isRealModeRequested()).isTrue();
        assertThat(factory.getAdapterByName("PAYOS")).isNull();
    }
}
