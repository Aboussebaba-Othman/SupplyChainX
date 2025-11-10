package com.supplychainx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.supplychainx")
@EntityScan(basePackages = {
    "com.supplychainx.security.entity",
    "com.supplychainx.supply.entity",
    "com.supplychainx.production.entity",
    "com.supplychainx.delivery.entity",
    "com.supplychainx.audit.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.supplychainx.security.repository",
    "com.supplychainx.supply.repository",
    "com.supplychainx.production.repository",
    "com.supplychainx.delivery.repository",
    "com.supplychainx.audit.repository"
})
@EnableScheduling
public class SupplyChainXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyChainXApplication.class, args);
    }
}
