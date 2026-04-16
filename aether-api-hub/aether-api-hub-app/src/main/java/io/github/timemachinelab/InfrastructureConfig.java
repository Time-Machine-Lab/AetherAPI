package io.github.timemachinelab;

import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.domain.catalog.repository.ApiAssetRepository;
import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.domain.consumerauth.repository.ApiCredentialRepository;
import io.github.timemachinelab.domain.consumerauth.repository.ConsumerIdentityRepository;
import io.github.timemachinelab.domain.consumerauth.repository.UserConsumerMappingRepository;
import io.github.timemachinelab.service.adapter.ApiAssetRepositoryAdapter;
import io.github.timemachinelab.service.adapter.ApiCredentialRepositoryAdapter;
import io.github.timemachinelab.service.adapter.CategoryValidityAdapter;
import io.github.timemachinelab.service.adapter.ConsumerIdentityRepositoryAdapter;
import io.github.timemachinelab.service.application.ApiAssetApplicationService;
import io.github.timemachinelab.service.application.ApiCredentialApplicationService;
import io.github.timemachinelab.service.application.CatalogDiscoveryApplicationService;
import io.github.timemachinelab.service.application.CredentialValidationApplicationService;
import io.github.timemachinelab.service.adapter.CategoryRepositoryAdapter;
import io.github.timemachinelab.service.adapter.UserConsumerMappingRepositoryAdapter;
import io.github.timemachinelab.service.application.CategoryApplicationService;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.in.ApiCredentialUseCase;
import io.github.timemachinelab.service.port.in.CatalogDiscoveryUseCase;
import io.github.timemachinelab.service.port.in.CredentialValidationUseCase;
import io.github.timemachinelab.service.port.out.ApiCredentialRepositoryPort;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.CatalogDiscoveryQueryPort;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;
import io.github.timemachinelab.service.port.out.ConsumerIdentityRepositoryPort;
import io.github.timemachinelab.service.port.out.UserConsumerMappingRepositoryPort;
import io.github.timemachinelab.domain.consumerauth.service.CredentialValidationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            ApiAssetRepositoryPort apiAssetRepositoryPort, CategoryValidityChecker categoryValidityChecker) {
        return new ApiAssetApplicationService(apiAssetRepositoryPort, categoryValidityChecker);
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
}
