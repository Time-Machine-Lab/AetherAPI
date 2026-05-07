package io.github.timemachinelab.domain.platformproxy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformProxyProfileAggregateTest {

    @Test
    @DisplayName("create should reject invalid proxy port")
    void shouldRejectInvalidProxyPort() {
        assertThrows(IllegalArgumentException.class, () -> PlatformProxyProfileAggregate.create(
                PlatformProxyProfileId.generate(),
                "openai-egress",
                "OpenAI Egress",
                ProxyType.HTTP,
                "127.0.0.1",
                70000,
                null,
                null,
                true
        ));
    }

    @Test
    @DisplayName("disabled and deleted profiles cannot be bound")
    void shouldReportBindingAvailabilityByState() {
        PlatformProxyProfileAggregate aggregate = PlatformProxyProfileAggregate.create(
                PlatformProxyProfileId.generate(),
                "default-cn",
                "Default CN",
                ProxyType.HTTP,
                "127.0.0.1",
                7890,
                "proxy-user",
                "secret",
                true
        );

        assertTrue(aggregate.canBeBound());
        assertTrue(aggregate.hasCredential());

        aggregate.disable();
        assertFalse(aggregate.canBeBound());

        aggregate.enable();
        aggregate.softDelete();
        assertFalse(aggregate.canBeBound());
        assertThrows(PlatformProxyProfileDomainException.class, aggregate::enable);
    }
}
