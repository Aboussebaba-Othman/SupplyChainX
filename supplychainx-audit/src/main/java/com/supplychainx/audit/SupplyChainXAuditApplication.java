package com.supplychainx.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Application principale du module Audit de SupplyChainX
 * 
 * Ce module gère :
 * - Les logs d'audit de toutes les actions du système
 * - Les alertes de stock (matières premières et produits finis)
 * - L'envoi automatique d'emails pour les alertes critiques
 * - La planification des vérifications de stock
 */
@SpringBootApplication(scanBasePackages = {
        "com.supplychainx.audit",
        "com.supplychainx.common"
})
@EnableJpaAuditing
public class SupplyChainXAuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyChainXAuditApplication.class, args);
    }

}
