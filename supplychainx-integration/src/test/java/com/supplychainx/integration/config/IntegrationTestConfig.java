package com.supplychainx.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration
@ActiveProfiles("test")
public class IntegrationTestConfig {

    /**
     * Conteneur MySQL pour les tests d'intégration
     */
    @Bean
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("test_supplychainx_db")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        
        container.start();
        return container;
    }

    /**
     * Password encoder pour les tests
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
