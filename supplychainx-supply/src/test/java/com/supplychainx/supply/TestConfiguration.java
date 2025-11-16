package com.supplychainx.supply;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Lightweight test configuration for the supply module.
 * Keep this as a plain @Configuration to avoid triggering full
 * Spring Boot auto-configuration during @WebMvcTest slices. Tests
 * explicitly import this class when they need to register module
 * beans or component scanning.
 */
@Configuration
@ComponentScan(basePackages = {
    // Limit component scanning to the supply module for lightweight test contexts.
    "com.supplychainx.supply"
})
public class TestConfiguration {

}
