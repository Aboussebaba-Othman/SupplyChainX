package com.supplychainx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.supplychainx",
    "com.supplychainx.security",
    "com.supplychainx.supply",
    "com.supplychainx.production",
    "com.supplychainx.delivery",
    "com.supplychainx.common"
})
@EntityScan(basePackages = {
    "com.supplychainx.security.entity",
    "com.supplychainx.supply.entity",
    "com.supplychainx.production.entity",
    "com.supplychainx.delivery.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.supplychainx.security.repository",
    "com.supplychainx.supply.repository",
    "com.supplychainx.production.repository",
    "com.supplychainx.delivery.repository"
})
public class SupplyChainXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyChainXApplication.class, args);
    }
}
