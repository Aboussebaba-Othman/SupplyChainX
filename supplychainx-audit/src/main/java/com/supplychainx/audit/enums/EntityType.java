package com.supplychainx.audit.enums;

/**
 * Types d'entités auditées dans le système
 */
public enum EntityType {
    // Module Approvisionnement
    SUPPLIER,
    RAW_MATERIAL,
    SUPPLY_ORDER,
    SUPPLY_ORDER_LINE,
    
    // Module Production
    PRODUCT,
    BILL_OF_MATERIAL,
    PRODUCTION_ORDER,
    
    // Module Livraison
    CUSTOMER,
    ORDER,
    DELIVERY,
    
    // Sécurité
    USER,
    
    // Système
    SYSTEM
}
