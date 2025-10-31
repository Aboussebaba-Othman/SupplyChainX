package com.supplychainx.production.enums;

// Enum représentant les différents statuts d'un ordre de production
public enum ProductionOrderStatus {
    PLANIFIE,    // Ordre planifié mais pas encore démarré
    EN_COURS,    // Production en cours
    TERMINE,     // Production terminée avec succès
    ANNULE       // Ordre annulé
}
