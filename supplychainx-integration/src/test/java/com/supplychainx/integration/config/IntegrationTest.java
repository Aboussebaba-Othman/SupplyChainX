package com.supplychainx.integration.config;

import com.supplychainx.SupplyChainXApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe de base abstraite pour les tests d'intégration avec TestContainers
 * 
 * Usage:
 * <pre>
 * class MyIntegrationTest extends IntegrationTest {
 *     // Test methods...
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = SupplyChainXApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTest {
    
    /**
     * Conteneur MySQL partagé pour tous les tests d'intégration
     */
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = 
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("test_supplychainx_db")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
    
    /**
     * Configuration dynamique des propriétés depuis le conteneur
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }
}
