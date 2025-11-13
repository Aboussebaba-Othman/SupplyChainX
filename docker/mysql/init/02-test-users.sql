-- ================================================================================
-- Script SQL - Création des Utilisateurs de Test pour SupplyChainX
-- ================================================================================
-- Ce script crée des utilisateurs avec différents rôles pour tester les permissions
-- Mot de passe par défaut pour tous : "password123" (BCrypt encoded)
-- ================================================================================

-- Nettoyer les données existantes (optionnel)
-- DELETE FROM users;

-- ================================================================================
-- 1. ADMIN - Toutes les permissions
-- ================================================================================
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'admin',
    'admin@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy', -- password123
    'ADMIN',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ================================================================================
-- 2. MODULE SUPPLY CHAIN - 3 rôles
-- ================================================================================

-- 2.1 GESTIONNAIRE_APPROVISIONNEMENT - Full CRUD Suppliers & Raw Materials
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'gestionnaire',
    'gestionnaire@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'GESTIONNAIRE_APPROVISIONNEMENT',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2.2 RESPONSABLE_ACHATS - Purchase Orders Manager
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'responsable_achats',
    'achats@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'RESPONSABLE_ACHATS',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2.3 SUPERVISEUR_LOGISTIQUE - Read-only Supply Chain
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'superviseur_log',
    'superviseur.log@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'SUPERVISEUR_LOGISTIQUE',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ================================================================================
-- 3. MODULE PRODUCTION - 3 rôles
-- ================================================================================

-- 3.1 CHEF_PRODUCTION - Full CRUD Products & Production Orders
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'chef_prod',
    'chef.prod@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'CHEF_PRODUCTION',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 3.2 PLANIFICATEUR - Production Planning
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'planificateur',
    'planificateur@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'PLANIFICATEUR',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 3.3 SUPERVISEUR_PRODUCTION - Production Monitoring
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'superviseur_prod',
    'superviseur.prod@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'SUPERVISEUR_PRODUCTION',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ================================================================================
-- 4. MODULE DELIVERY - 3 rôles
-- ================================================================================

-- 4.1 GESTIONNAIRE_COMMERCIAL - Sales Manager
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'commercial',
    'commercial@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'GESTIONNAIRE_COMMERCIAL',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4.2 RESPONSABLE_LOGISTIQUE - Logistics Manager
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'resp_logistique',
    'resp.logistique@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'RESPONSABLE_LOGISTIQUE',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4.3 SUPERVISEUR_LIVRAISONS - Delivery Monitoring
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'superviseur_livraisons',
    'superviseur.livraisons@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'SUPERVISEUR_LIVRAISONS',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ================================================================================
-- 5. UTILISATEURS DE TEST SUPPLÉMENTAIRES
-- ================================================================================

-- 5.1 Compte désactivé (pour tester le refus de connexion)
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, created_at, updated_at)
VALUES (
    'user_disabled',
    'disabled@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'SUPERVISEUR_LOGISTIQUE',
    false, -- Désactivé
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.2 Compte verrouillé (pour tester le mécanisme de verrouillage)
INSERT INTO users (username, email, password, role, enabled, account_non_locked, failed_login_attempts, lock_time, created_at, updated_at)
VALUES (
    'user_locked',
    'locked@supplychainx.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y5myN0aR0Emy',
    'SUPERVISEUR_LOGISTIQUE',
    true,
    false, -- Verrouillé
    5, -- 5 tentatives échouées
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ================================================================================
-- VÉRIFICATION - Lister tous les utilisateurs créés
-- ================================================================================
SELECT 
    id,
    username,
    email,
    role,
    enabled,
    account_non_locked,
    failed_login_attempts
FROM users
ORDER BY role, username;

-- ================================================================================
-- RÉSUMÉ DES UTILISATEURS
-- ================================================================================
/*
┌─────────────────────────┬──────────────────────┬───────────────────────────────┐
│ Username                │ Role                 │ Permissions Principales       │
├─────────────────────────┼──────────────────────┼───────────────────────────────┤
│ admin                   │ ADMIN                │ TOUTES                        │
├─────────────────────────┼──────────────────────┼───────────────────────────────┤
│ gestionnaire            │ GEST_APPRO           │ SUPPLIER_*, RAW_MATERIAL_*    │
│ responsable_achats      │ RESP_ACHATS          │ PURCHASE_ORDER_*              │
│ superviseur_log         │ SUPER_LOGISTIQUE     │ READ-ONLY supply              │
├─────────────────────────┼──────────────────────┼───────────────────────────────┤
│ chef_prod               │ CHEF_PRODUCTION      │ PRODUCT_*, PRODUCTION_ORDER_* │
│ planificateur           │ PLANIFICATEUR        │ PRODUCTION_ORDER_CREATE/READ  │
│ superviseur_prod        │ SUPER_PRODUCTION     │ PRODUCTION_ORDER_START/COMPLETE│
├─────────────────────────┼──────────────────────┼───────────────────────────────┤
│ commercial              │ GEST_COMMERCIAL      │ CUSTOMER_*, DELIVERY_ORDER_*  │
│ resp_logistique         │ RESP_LOGISTIQUE      │ DELIVERY_*                    │
│ superviseur_livraisons  │ SUPER_LIVRAISONS     │ DELIVERY_STATUS_UPDATE        │
├─────────────────────────┼──────────────────────┼───────────────────────────────┤
│ user_disabled           │ (any)                │ COMPTE DÉSACTIVÉ              │
│ user_locked             │ (any)                │ COMPTE VERROUILLÉ             │
└─────────────────────────┴──────────────────────┴───────────────────────────────┘

🔑 Mot de passe pour tous : password123
📧 Format email : <role>@supplychainx.com
*/
