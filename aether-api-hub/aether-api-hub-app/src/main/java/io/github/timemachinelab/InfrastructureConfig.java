package io.github.timemachinelab;

import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.domain.catalog.repository.ApiAssetRepository;
import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.domain.consumerauth.repository.ApiCredentialRepository;
import io.github.timemachinelab.domain.consumerauth.repository.ConsumerIdentityRepository;
import io.github.timemachinelab.domain.consumerauth.repository.UserConsumerMappingRepository;
import io.github.timemachinelab.domain.observability.repository.ApiCallLogRepository;
import io.github.timemachinelab.domain.subscription.repository.ApiSubscriptionRepository;
import io.github.timemachinelab.infrastructure.external.unifiedaccess.JdkUnifiedAccessDownstreamProxyPort;
import io.github.timemachinelab.service.adapter.ApiCallLogRepositoryAdapter;
import io.github.timemachinelab.service.adapter.ApiAssetRepositoryAdapter;
import io.github.timemachinelab.service.adapter.ApiCredentialRepositoryAdapter;
import io.github.timemachinelab.service.adapter.ApiSubscriptionRepositoryAdapter;
import io.github.timemachinelab.service.adapter.CategoryValidityAdapter;
import io.github.timemachinelab.service.adapter.ConsumerIdentityRepositoryAdapter;
import io.github.timemachinelab.service.application.ApiCallLogApplicationService;
import io.github.timemachinelab.service.application.ApiAssetApplicationService;
import io.github.timemachinelab.service.application.ApiCredentialApplicationService;
import io.github.timemachinelab.service.application.ApiSubscriptionApplicationService;
import io.github.timemachinelab.service.application.CatalogDiscoveryApplicationService;
import io.github.timemachinelab.service.application.ConsoleSessionAuthApplicationService;
import io.github.timemachinelab.service.application.CredentialValidationApplicationService;
import io.github.timemachinelab.service.application.ObservabilityApplicationService;
import io.github.timemachinelab.service.application.UnifiedAccessApplicationService;
import io.github.timemachinelab.service.adapter.CategoryRepositoryAdapter;
import io.github.timemachinelab.service.adapter.UserConsumerMappingRepositoryAdapter;
import io.github.timemachinelab.service.application.CategoryApplicationService;
import io.github.timemachinelab.service.port.in.ObservabilityUseCase;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.in.ApiCallLogUseCase;
import io.github.timemachinelab.service.port.in.ApiCredentialUseCase;
import io.github.timemachinelab.service.port.in.ApiSubscriptionUseCase;
import io.github.timemachinelab.service.port.in.CatalogDiscoveryUseCase;
import io.github.timemachinelab.service.port.in.ConsoleSessionAuthUseCase;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.in.UnifiedAccessUseCase;
import io.github.timemachinelab.service.port.out.ApiCallLogRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiAssetQueryPort;
import io.github.timemachinelab.service.port.out.ApiCallLogQueryPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionEntitlementPort;
import io.github.timemachinelab.service.port.out.ApiSubscriptionRepositoryPort;
import io.github.timemachinelab.service.port.out.CatalogDiscoveryQueryPort;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsoleSessionSettingsPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import io.github.timemachinelab.service.port.out.UnifiedAccessDownstreamProxyPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;
import io.github.timemachinelab.domain.consumerauth.service.CredentialValidationDomainService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Infrastructure layer configuration class.
 */
@Configuration
public class InfrastructureConfig {

    /**
     * Register category repository port adapter.
     */
    @Bean
    public CategoryRepositoryPort categoryRepositoryPort(ApiCategoryRepository apiCategoryRepository) {
        return new CategoryRepositoryAdapter(apiCategoryRepository);
    }

    /**
     * Register category application service.
     */
    @Bean
    public io.github.timemachinelab.service.port.in.CategoryUseCase categoryUseCase(
            CategoryRepositoryPort categoryRepositoryPort) {
        return new CategoryApplicationService(categoryRepositoryPort);
    }

    @Bean
    public ApiAssetRepositoryPort apiAssetRepositoryPort(ApiAssetRepository apiAssetRepository) {
        return new ApiAssetRepositoryAdapter(apiAssetRepository);
    }

    @Bean
    public CategoryValidityChecker categoryValidityChecker(CategoryRepositoryPort categoryRepositoryPort) {
        return new CategoryValidityAdapter(categoryRepositoryPort);
    }

    @Bean
    public ApiAssetUseCase apiAssetUseCase(
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            ApiAssetQueryPort apiAssetQueryPort,
            CategoryValidityChecker categoryValidityChecker) {
        return new ApiAssetApplicationService(apiAssetRepositoryPort, apiAssetQueryPort, categoryValidityChecker);
    }

    @Bean
    public ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort(ConsumerIdentityRepository consumerIdentityRepository) {
        return new ConsumerIdentityRepositoryAdapter(consumerIdentityRepository);
    }

    @Bean
    public UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort(
            UserConsumerMappingRepository userConsumerMappingRepository) {
        return new UserConsumerMappingRepositoryAdapter(userConsumerMappingRepository);
    }

    @Bean
    public ApiCredentialRepositoryPort apiCredentialRepositoryPort(ApiCredentialRepository apiCredentialRepository) {
        return new ApiCredentialRepositoryAdapter(apiCredentialRepository);
    }

    @Bean
    public ApiSubscriptionRepositoryAdapter apiSubscriptionRepositoryAdapter(ApiSubscriptionRepository apiSubscriptionRepository) {
        return new ApiSubscriptionRepositoryAdapter(apiSubscriptionRepository);
    }

    @Bean
    public ApiCallLogRepositoryPort apiCallLogRepositoryPort(ApiCallLogRepository apiCallLogRepository) {
        return new ApiCallLogRepositoryAdapter(apiCallLogRepository);
    }

    @Bean
    public ApiCredentialUseCase apiCredentialUseCase(
            ApiCredentialRepositoryPort apiCredentialRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        return new ApiCredentialApplicationService(
                apiCredentialRepositoryPort,
                consumerIdentityRepositoryPort,
                userConsumerMappingRepositoryPort
        );
    }

    @Bean
    public ApiSubscriptionUseCase apiSubscriptionUseCase(
            ApiSubscriptionRepositoryPort apiSubscriptionRepositoryPort,
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        return new ApiSubscriptionApplicationService(
                apiSubscriptionRepositoryPort,
                apiAssetRepositoryPort,
                consumerIdentityRepositoryPort,
                userConsumerMappingRepositoryPort
        );
    }

    @Bean
    @ConfigurationProperties(prefix = "aether.console.session-auth")
    public ConsoleSessionAuthProperties consoleSessionAuthProperties() {
        return new ConsoleSessionAuthProperties();
    }

    @Bean
    public ConsoleSessionSettingsPort consoleSessionSettingsPort(ConsoleSessionAuthProperties consoleSessionAuthProperties) {
        return consoleSessionAuthProperties;
    }

    @Bean
    public ConsoleSessionAuthUseCase consoleSessionAuthUseCase(ConsoleSessionSettingsPort consoleSessionSettingsPort) {
        return new ConsoleSessionAuthApplicationService(consoleSessionSettingsPort);
    }

    @Bean
    public ApiCallLogUseCase apiCallLogUseCase(
            ApiCallLogQueryPort apiCallLogQueryPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort) {
        return new ApiCallLogApplicationService(apiCallLogQueryPort, userConsumerMappingRepositoryPort);
    }

    @Bean
    public CredentialValidationDomainService credentialValidationDomainService() {
        return new CredentialValidationDomainService();
    }

    @Bean
    public CredentialValidationUseCase credentialValidationUseCase(
            ApiCredentialRepositoryPort apiCredentialRepositoryPort,
            ConsumerIdentityRepositoryPort consumerIdentityRepositoryPort,
            CredentialValidationDomainService credentialValidationDomainService) {
        return new CredentialValidationApplicationService(
                apiCredentialRepositoryPort,
                consumerIdentityRepositoryPort,
                credentialValidationDomainService
        );
    }

    @Bean
    public CatalogDiscoveryUseCase catalogDiscoveryUseCase(CatalogDiscoveryQueryPort catalogDiscoveryQueryPort) {
        return new CatalogDiscoveryApplicationService(catalogDiscoveryQueryPort);
    }

    @Bean
    public ObservabilityUseCase observabilityUseCase(ApiCallLogRepositoryPort apiCallLogRepositoryPort) {
        return new ObservabilityApplicationService(apiCallLogRepositoryPort);
    }

    @Bean
    public HttpClient unifiedAccessHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Bean
    public UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort(HttpClient unifiedAccessHttpClient) {
        return new JdkUnifiedAccessDownstreamProxyPort(unifiedAccessHttpClient);
    }

    @Bean
    public UnifiedAccessUseCase unifiedAccessUseCase(
            CredentialValidationUseCase credentialValidationUseCase,
            ApiAssetRepositoryPort apiAssetRepositoryPort,
            ApiSubscriptionEntitlementPort apiSubscriptionEntitlementPort,
            UserConsumerMappingRepositoryPort userConsumerMappingRepositoryPort,
            UnifiedAccessDownstreamProxyPort unifiedAccessDownstreamProxyPort,
            ObservabilityUseCase observabilityUseCase) {
        return new UnifiedAccessApplicationService(
                credentialValidationUseCase,
                apiAssetRepositoryPort,
                apiSubscriptionEntitlementPort,
                userConsumerMappingRepositoryPort,
                unifiedAccessDownstreamProxyPort,
                observabilityUseCase
        );
    }
}

