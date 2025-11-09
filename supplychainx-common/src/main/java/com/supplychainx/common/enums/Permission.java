package com.supplychainx.common.enums;

public enum Permission {
    
    // ==================== SUPPLY MODULE ====================
    // Supplier permissions
    SUPPLIER_CREATE("Créer des fournisseurs"),
    SUPPLIER_READ("Consulter des fournisseurs"),
    SUPPLIER_UPDATE("Modifier des fournisseurs"),
    SUPPLIER_DELETE("Supprimer des fournisseurs"),
    
    // Purchase Order permissions
    PURCHASE_ORDER_CREATE("Créer des commandes d'achat"),
    PURCHASE_ORDER_READ("Consulter des commandes d'achat"),
    PURCHASE_ORDER_UPDATE("Modifier des commandes d'achat"),
    PURCHASE_ORDER_DELETE("Supprimer des commandes d'achat"),
    PURCHASE_ORDER_APPROVE("Approuver des commandes d'achat"),
    PURCHASE_ORDER_RECEIVE("Réceptionner des commandes"),
    
    // Raw Material permissions
    RAW_MATERIAL_CREATE("Créer des matières premières"),
    RAW_MATERIAL_READ("Consulter des matières premières"),
    RAW_MATERIAL_UPDATE("Modifier des matières premières"),
    RAW_MATERIAL_DELETE("Supprimer des matières premières"),
    RAW_MATERIAL_STOCK_UPDATE("Mettre à jour le stock de matières premières"),
    
    // ==================== PRODUCTION MODULE ====================
    // Product permissions
    PRODUCT_CREATE("Créer des produits"),
    PRODUCT_READ("Consulter des produits"),
    PRODUCT_UPDATE("Modifier des produits"),
    PRODUCT_DELETE("Supprimer des produits"),
    
    // Production Order permissions
    PRODUCTION_ORDER_CREATE("Créer des ordres de production"),
    PRODUCTION_ORDER_READ("Consulter des ordres de production"),
    PRODUCTION_ORDER_UPDATE("Modifier des ordres de production"),
    PRODUCTION_ORDER_DELETE("Supprimer des ordres de production"),
    PRODUCTION_ORDER_START("Démarrer la production"),
    PRODUCTION_ORDER_COMPLETE("Terminer la production"),
    PRODUCTION_ORDER_CANCEL("Annuler des ordres de production"),
    
    // Production Line permissions
    PRODUCTION_LINE_CREATE("Créer des lignes de production"),
    PRODUCTION_LINE_READ("Consulter des lignes de production"),
    PRODUCTION_LINE_UPDATE("Modifier des lignes de production"),
    PRODUCTION_LINE_DELETE("Supprimer des lignes de production"),
    
    // ==================== DELIVERY MODULE ====================
    // Customer permissions
    CUSTOMER_CREATE("Créer des clients"),
    CUSTOMER_READ("Consulter des clients"),
    CUSTOMER_UPDATE("Modifier des clients"),
    CUSTOMER_DELETE("Supprimer des clients"),
    
    // Delivery Order permissions
    DELIVERY_ORDER_CREATE("Créer des commandes de livraison"),
    DELIVERY_ORDER_READ("Consulter des commandes de livraison"),
    DELIVERY_ORDER_UPDATE("Modifier des commandes de livraison"),
    DELIVERY_ORDER_DELETE("Supprimer des commandes de livraison"),
    DELIVERY_ORDER_VALIDATE("Valider des commandes de livraison"),
    
    // Delivery permissions
    DELIVERY_CREATE("Créer des livraisons"),
    DELIVERY_READ("Consulter des livraisons"),
    DELIVERY_UPDATE("Modifier des livraisons"),
    DELIVERY_DELETE("Supprimer des livraisons"),
    DELIVERY_STATUS_UPDATE("Mettre à jour le statut des livraisons"),
    DELIVERY_COMPLETE("Marquer des livraisons comme livrées"),
    
    // ==================== ADMIN & AUDIT ====================
    USER_MANAGEMENT("Gérer les utilisateurs"),
    AUDIT_READ("Consulter les logs d'audit"),
    SYSTEM_CONFIG("Configurer le système");
    
    private final String description;
    
    Permission(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne le nom de la permission formaté pour Spring Security
     * Ex: SUPPLIER_CREATE -> PERM_SUPPLIER_CREATE
     */
    public String getAuthority() {
        return "PERM_" + this.name();
    }
}
