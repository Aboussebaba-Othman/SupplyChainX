package com.supplychainx.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;


@TestConfiguration
@ActiveProfiles("test")
public class IntegrationTestConfig {

    // Intentionally empty: integration tests use the static Testcontainers MySQL container
    // defined in the abstract IntegrationTest base class. Avoid defining another container bean
    // or duplicating beans from the main application context to prevent startup conflicts.

}
