package com.supplychainx.supply;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration de test pour le module Supply
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.supplychainx.supply",
    "com.supplychainx.security",
    "com.supplychainx.common"
})
public class TestConfiguration {
}
