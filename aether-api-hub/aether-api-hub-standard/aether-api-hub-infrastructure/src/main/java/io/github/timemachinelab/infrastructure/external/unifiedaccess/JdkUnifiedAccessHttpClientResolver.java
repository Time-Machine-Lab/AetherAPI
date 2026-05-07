package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import io.github.timemachinelab.service.model.ProxyProfileSnapshotModel;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDK HTTP client resolver supporting direct and HTTP-proxied outbound calls.
 */
public class JdkUnifiedAccessHttpClientResolver implements UnifiedAccessHttpClientResolver {

    private final HttpClient directClient;
    private final Duration connectTimeout;
    private final Map<String, HttpClient> proxiedClients = new ConcurrentHashMap<>();

    public JdkUnifiedAccessHttpClientResolver(HttpClient directClient, Duration connectTimeout) {
        this.directClient = Objects.requireNonNull(directClient, "Direct HTTP client must not be null");
        this.connectTimeout = connectTimeout == null ? Duration.ofSeconds(10) : connectTimeout;
    }

    @Override
    public HttpClient resolve(ProxyProfileSnapshotModel proxyProfile) {
        if (proxyProfile == null) {
            return directClient;
        }
        return proxiedClients.computeIfAbsent(cacheKey(proxyProfile), ignored -> buildProxiedClient(proxyProfile));
    }

    private HttpClient buildProxiedClient(ProxyProfileSnapshotModel proxyProfile) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .proxy(ProxySelector.of(new InetSocketAddress(proxyProfile.getProxyHost(), proxyProfile.getProxyPort())));
        if (proxyProfile.getUsername() != null && proxyProfile.getPasswordSecret() != null) {
            builder.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            proxyProfile.getUsername(),
                            proxyProfile.getPasswordSecret().toCharArray()
                    );
                }
            });
        }
        return builder.build();
    }

    private String cacheKey(ProxyProfileSnapshotModel proxyProfile) {
        return proxyProfile.getProfileId() + ":" + proxyProfile.getVersion();
    }
}
