package io.github.aetherapihub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("io.github.aetherapihub.catalog.infrastructure.persistence.mapper")
public class AetherApiHubApp {
    public static void main(String[] args) {
        SpringApplication.run(AetherApiHubApp.class, args);
    }
}
