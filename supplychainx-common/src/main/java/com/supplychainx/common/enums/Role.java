package com.supplychainx.common.enums;

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
