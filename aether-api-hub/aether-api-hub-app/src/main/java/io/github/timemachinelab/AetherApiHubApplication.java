package io.github.timemachinelab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aether API Hub application entry point.
 */
@SpringBootApplication
@MapperScan({
        "io.github.timemachinelab.infrastructure.catalog.persistence.mapper",
        "io.github.timemachinelab.infrastructure.catalog.persistence.query",
        "io.github.timemachinelab.infrastructure.consumerauth.persistence.mapper",
    "io.github.timemachinelab.infrastructure.importagent.persistence.mapper",
        "io.github.timemachinelab.infrastructure.observability.persistence.mapper",
        "io.github.timemachinelab.infrastructure.observability.persistence.query",
        "io.github.timemachinelab.infrastructure.platformproxy.persistence.mapper",
        "io.github.timemachinelab.infrastructure.subscription.persistence.mapper"
})
public class AetherApiHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AetherApiHubApplication.class, args);
    }
}

