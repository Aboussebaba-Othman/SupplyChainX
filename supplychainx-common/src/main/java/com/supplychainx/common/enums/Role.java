package com.supplychainx.common.enums;

import java.util.Set;
import java.util.EnumSet;

public enum Role {
    ADMIN("Administrator - Full system access"),

    GESTIONNAIRE_APPROVISIONNEMENT("Supply Manager - Manage suppliers and materials"),
    RESPONSABLE_ACHATS("Purchase Manager - Manage supply orders"),
    SUPERVISEUR_LOGISTIQUE("Logistics Supervisor - Monitor supply chain"),

    // Production Module
    CHEF_PRODUCTION("Production Manager - Manage products and production orders"),
    PLANIFICATEUR("Planner - Production planning and scheduling"),
    SUPERVISEUR_PRODUCTION("Production Supervisor - Monitor production"),

    // Delivery Module
    GESTIONNAIRE_COMMERCIAL("Sales Manager - Manage customers and orders"),
    RESPONSABLE_LOGISTIQUE("Logistics Manager - Manage deliveries"),
    SUPERVISEUR_LIVRAISONS("Delivery Supervisor - Monitor deliveries");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne l'ensemble des permissions pour ce rôle
     */
    public Set<Permission> getPermissions() {
        return switch (this) {
            case ADMIN -> EnumSet.allOf(Permission.class); // Admin a toutes les permissions
            
            // ========== SUPPLY MODULE ==========
            case GESTIONNAIRE_APPROVISIONNEMENT -> EnumSet.of(
                // Supplier - Full CRUD
                Permission.SUPPLIER_CREATE,
                Permission.SUPPLIER_READ,
                Permission.SUPPLIER_UPDATE,
                Permission.SUPPLIER_DELETE,
                // Raw Material - Full CRUD + Stock
                Permission.RAW_MATERIAL_CREATE,
                Permission.RAW_MATERIAL_READ,
                Permission.RAW_MATERIAL_UPDATE,
                Permission.RAW_MATERIAL_DELETE,
                Permission.RAW_MATERIAL_STOCK_UPDATE,
                // Purchase Order - Read only (approve is for RESPONSABLE_ACHATS)
                Permission.PURCHASE_ORDER_READ
            );
            
            case RESPONSABLE_ACHATS -> EnumSet.of(
                // Supplier - Read only
                Permission.SUPPLIER_READ,
                // Purchase Order - Full control
                Permission.PURCHASE_ORDER_CREATE,
                Permission.PURCHASE_ORDER_READ,
                Permission.PURCHASE_ORDER_UPDATE,
                Permission.PURCHASE_ORDER_DELETE,
                Permission.PURCHASE_ORDER_APPROVE,
                Permission.PURCHASE_ORDER_RECEIVE,
                // Raw Material - Read + Stock update
                Permission.RAW_MATERIAL_READ,
                Permission.RAW_MATERIAL_STOCK_UPDATE
            );
            
            case SUPERVISEUR_LOGISTIQUE -> EnumSet.of(
                // Read-only access to monitor supply chain
                Permission.SUPPLIER_READ,
                Permission.PURCHASE_ORDER_READ,
                Permission.RAW_MATERIAL_READ,
                // Can receive orders
                Permission.PURCHASE_ORDER_RECEIVE
            );
            
            // ========== PRODUCTION MODULE ==========
            case CHEF_PRODUCTION -> EnumSet.of(
                // Product - Full CRUD
                Permission.PRODUCT_CREATE,
                Permission.PRODUCT_READ,
                Permission.PRODUCT_UPDATE,
                Permission.PRODUCT_DELETE,
                // Production Order - Full control
                Permission.PRODUCTION_ORDER_CREATE,
                Permission.PRODUCTION_ORDER_READ,
                Permission.PRODUCTION_ORDER_UPDATE,
                Permission.PRODUCTION_ORDER_DELETE,
                Permission.PRODUCTION_ORDER_START,
                Permission.PRODUCTION_ORDER_COMPLETE,
                Permission.PRODUCTION_ORDER_CANCEL,
                // Production Line - Full CRUD
                Permission.PRODUCTION_LINE_CREATE,
                Permission.PRODUCTION_LINE_READ,
                Permission.PRODUCTION_LINE_UPDATE,
                Permission.PRODUCTION_LINE_DELETE,
                // Raw Material - Read access
                Permission.RAW_MATERIAL_READ
            );
            
            case PLANIFICATEUR -> EnumSet.of(
                // Product - Read only
                Permission.PRODUCT_READ,
                // Production Order - Create and manage
                Permission.PRODUCTION_ORDER_CREATE,
                Permission.PRODUCTION_ORDER_READ,
                Permission.PRODUCTION_ORDER_UPDATE,
                Permission.PRODUCTION_ORDER_CANCEL,
                // Production Line - Read only
                Permission.PRODUCTION_LINE_READ,
                // Raw Material - Read to check availability
                Permission.RAW_MATERIAL_READ
            );
            
            case SUPERVISEUR_PRODUCTION -> EnumSet.of(
                // Product - Read only
                Permission.PRODUCT_READ,
                // Production Order - Read + operational actions
                Permission.PRODUCTION_ORDER_READ,
                Permission.PRODUCTION_ORDER_START,
                Permission.PRODUCTION_ORDER_COMPLETE,
                // Production Line - Read only
                Permission.PRODUCTION_LINE_READ,
                // Raw Material - Read access
                Permission.RAW_MATERIAL_READ
            );
            
            // ========== DELIVERY MODULE ==========
            case GESTIONNAIRE_COMMERCIAL -> EnumSet.of(
                // Customer - Full CRUD
                Permission.CUSTOMER_CREATE,
                Permission.CUSTOMER_READ,
                Permission.CUSTOMER_UPDATE,
                Permission.CUSTOMER_DELETE,
                // Delivery Order - Full control
                Permission.DELIVERY_ORDER_CREATE,
                Permission.DELIVERY_ORDER_READ,
                Permission.DELIVERY_ORDER_UPDATE,
                Permission.DELIVERY_ORDER_DELETE,
                Permission.DELIVERY_ORDER_VALIDATE,
                // Delivery - Read access
                Permission.DELIVERY_READ,
                // Product - Read access
                Permission.PRODUCT_READ
            );
            
            case RESPONSABLE_LOGISTIQUE -> EnumSet.of(
                // Customer - Read only
                Permission.CUSTOMER_READ,
                // Delivery Order - Read + Validate
                Permission.DELIVERY_ORDER_READ,
                Permission.DELIVERY_ORDER_VALIDATE,
                // Delivery - Full control
                Permission.DELIVERY_CREATE,
                Permission.DELIVERY_READ,
                Permission.DELIVERY_UPDATE,
                Permission.DELIVERY_DELETE,
                Permission.DELIVERY_STATUS_UPDATE,
                Permission.DELIVERY_COMPLETE,
                // Product - Read access
                Permission.PRODUCT_READ
            );
            
            case SUPERVISEUR_LIVRAISONS -> EnumSet.of(
                // Customer - Read only
                Permission.CUSTOMER_READ,
                // Delivery Order - Read only
                Permission.DELIVERY_ORDER_READ,
                // Delivery - Read + Update status + Complete
                Permission.DELIVERY_READ,
                Permission.DELIVERY_STATUS_UPDATE,
                Permission.DELIVERY_COMPLETE,
                // Product - Read access
                Permission.PRODUCT_READ
            );
        };
    }

    // Module-based permission checks
    public boolean canAccessSupplyModule() {
        return this == ADMIN ||
                this == GESTIONNAIRE_APPROVISIONNEMENT ||
                this == RESPONSABLE_ACHATS ||
                this == SUPERVISEUR_LOGISTIQUE;
    }

    public boolean canAccessProductionModule() {
        return this == ADMIN ||
                this == CHEF_PRODUCTION ||
                this == PLANIFICATEUR ||
                this == SUPERVISEUR_PRODUCTION;
    }

    public boolean canAccessDeliveryModule() {
        return this == ADMIN ||
                this == GESTIONNAIRE_COMMERCIAL ||
                this == RESPONSABLE_LOGISTIQUE ||
                this == SUPERVISEUR_LIVRAISONS;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }
}
