package io.github.timemachinelab;

import io.github.timemachinelab.domain.catalog.repository.ApiCategoryRepository;
import io.github.timemachinelab.service.adapter.CategoryRepositoryAdapter;
import io.github.timemachinelab.service.application.CategoryApplicationService;
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
}
