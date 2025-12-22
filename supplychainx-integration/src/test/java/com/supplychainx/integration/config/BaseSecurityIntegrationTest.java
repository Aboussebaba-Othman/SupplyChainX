package com.supplychainx.integration.config;

import com.supplychainx.security.repository.RefreshTokenRepository;
import com.supplychainx.security.repository.UserRepository;
import com.supplychainx.security.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

/**
 * Base class for security integration tests that provides database cleanup
 * before each test to ensure test isolation.
 */
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseSecurityIntegrationTest extends IntegrationTest {

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RateLimitingService rateLimitingService;

    // Liquibase users that should never be deleted
    private static final Set<String> LIQUIBASE_USERS = Set.of(
        "admin",
        "supply_manager",
        "purchase_manager",
        "logistics_supervisor",
        "production_manager",
        "planner",
        "production_supervisor",
        "sales_manager",
        "delivery_logistics",
        "delivery_supervisor"
    );

    @BeforeEach
    void cleanupDatabase() {
        // Clean up refresh tokens
        refreshTokenRepository.deleteAll();

        // Clean up users created by tests (keep the 10 initial users from Liquibase)
        userRepository.findAll().stream()
            .filter(user -> !LIQUIBASE_USERS.contains(user.getUsername()))
            .forEach(userRepository::delete);

        // Clear rate limiting cache to avoid "too many attempts" errors
        rateLimitingService.clearCache();
    }

    protected boolean isLiquibaseUser(String username) {
        return LIQUIBASE_USERS.contains(username);
    }
}
