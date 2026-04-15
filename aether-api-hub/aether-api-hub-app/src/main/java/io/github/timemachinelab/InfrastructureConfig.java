package io.github.timemachinelab;

import io.github.timemachinelab.domain.catalog.model.CategoryValidityChecker;
import io.github.timemachinelab.domain.catalog.repository.ApiAssetRepository;
import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.service.adapter.ApiAssetRepositoryAdapter;
import io.github.timemachinelab.service.adapter.CategoryValidityAdapter;
import io.github.timemachinelab.service.application.ApiAssetApplicationService;
import io.github.timemachinelab.service.adapter.CategoryRepositoryAdapter;
import io.github.timemachinelab.service.application.CategoryApplicationService;
import io.github.timemachinelab.service.port.in.ApiAssetUseCase;
import io.github.timemachinelab.service.port.out.ApiAssetRepositoryPort;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;
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
}
