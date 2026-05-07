package io.github.timemachinelab.infrastructure.external.unifiedaccess;

import io.github.timemachinelab.service.model.ProxyProfileSnapshotModel;

import java.net.http.HttpClient;

/**
 * Resolves the HTTP client used for one Unified Access outbound invocation.
 */
public interface UnifiedAccessHttpClientResolver {

    HttpClient resolve(ProxyProfileSnapshotModel proxyProfile);
}
