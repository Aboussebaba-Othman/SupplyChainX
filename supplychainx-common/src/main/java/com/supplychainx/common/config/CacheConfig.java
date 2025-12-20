package com.supplychainx.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration du cache pour l'application
 * 
 * En dev: utilise un cache en mémoire simple (ConcurrentHashMap)
 * En prod: peut être remplacé par Redis (voir application-prod.yml)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache en mémoire pour le développement
     * Utilisé quand le profil 'dev' est actif
     */
    @Bean
    @Profile("dev")
    public CacheManager cacheManagerDev() {
        return new ConcurrentMapCacheManager(
            "users",           // Cache pour les utilisateurs
            "products",        // Cache pour les produits
            "suppliers",       // Cache pour les fournisseurs
            "customers",       // Cache pour les clients
            "rawMaterials"     // Cache pour les matières premières
        );
    }

    // Pour ajouter Redis en production, décommenter et ajouter la dépendance:
    /*
    @Bean
    @Profile("prod")
    public CacheManager cacheManagerProd(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // TTL de 30 minutes
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
    */
}
