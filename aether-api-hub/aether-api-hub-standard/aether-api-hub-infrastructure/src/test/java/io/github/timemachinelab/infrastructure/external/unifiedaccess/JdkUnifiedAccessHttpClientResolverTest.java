package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import io.github.timemachinelab.service.model.ProxyProfileSnapshotModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdkUnifiedAccessHttpClientResolverTest {

    @Test
    @DisplayName("resolver should return direct client when no proxy profile is present")
    void shouldReturnDirectClientWithoutProxyProfile() {
        HttpClient direct = HttpClient.newHttpClient();
        JdkUnifiedAccessHttpClientResolver resolver = new JdkUnifiedAccessHttpClientResolver(direct, Duration.ofSeconds(1));

        assertSame(direct, resolver.resolve(null));
    }

    @Test
    @DisplayName("resolver should build and cache proxied client by profile version")
    void shouldBuildAndCacheProxiedClient() {
        HttpClient direct = HttpClient.newHttpClient();
        JdkUnifiedAccessHttpClientResolver resolver = new JdkUnifiedAccessHttpClientResolver(direct, Duration.ofSeconds(1));
        ProxyProfileSnapshotModel profile = new ProxyProfileSnapshotModel(
                "profile-1",
                "default-cn",
                "HTTP",
                "127.0.0.1",
                7890,
                "proxy-user",
                "proxy-secret",
                3L
        );

        HttpClient first = resolver.resolve(profile);
        HttpClient second = resolver.resolve(profile);

        assertSame(first, second);
        assertTrue(first.proxy().isPresent());
        assertTrue(first.authenticator().isPresent());
    }
}
