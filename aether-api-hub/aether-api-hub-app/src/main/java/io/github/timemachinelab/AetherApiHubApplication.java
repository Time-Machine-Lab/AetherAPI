package io.github.timemachinelab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aether API Hub application entry point.
 */
@SpringBootApplication
@MapperScan("io.github.timemachinelab.infrastructure")
public class AetherApiHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AetherApiHubApplication.class, args);
    }
}
