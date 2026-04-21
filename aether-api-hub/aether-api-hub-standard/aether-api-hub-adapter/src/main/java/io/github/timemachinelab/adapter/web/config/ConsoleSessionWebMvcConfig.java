package io.github.timemachinelab.adapter.web.config;

import io.github.timemachinelab.adapter.web.interceptor.ConsoleSessionAuthInterceptor;
import io.github.timemachinelab.adapter.web.resolver.ConsoleSessionPrincipalArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Console session MVC configuration.
 */
@Configuration
public class ConsoleSessionWebMvcConfig implements WebMvcConfigurer {

    private final ConsoleSessionAuthInterceptor consoleSessionAuthInterceptor;
    private final ConsoleSessionPrincipalArgumentResolver consoleSessionPrincipalArgumentResolver;

    public ConsoleSessionWebMvcConfig(
            ConsoleSessionAuthInterceptor consoleSessionAuthInterceptor,
            ConsoleSessionPrincipalArgumentResolver consoleSessionPrincipalArgumentResolver) {
        this.consoleSessionAuthInterceptor = consoleSessionAuthInterceptor;
        this.consoleSessionPrincipalArgumentResolver = consoleSessionPrincipalArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(consoleSessionAuthInterceptor)
                .addPathPatterns(
                        "/api/v1/current-user/**",
                        "/api/v1/console/auth/current-session"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(consoleSessionPrincipalArgumentResolver);
    }
}
