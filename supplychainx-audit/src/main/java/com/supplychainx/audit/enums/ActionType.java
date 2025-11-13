package com.supplychainx.audit.enums;

/**
 * Types d'actions auditées dans le système
 */
public enum ActionType {
    /**
     * Création d'une entité
     */
    CREATE,
    
    /**
     * Modification d'une entité
     */
    UPDATE,
    
    /**
     * Suppression d'une entité
     */
    DELETE,
    
    /**
     * Consultation d'une entité
     */
    READ,
    
    /**
     * Changement de statut
     */
    STATUS_CHANGE,
    
    /**
     * Login utilisateur
     */
    LOGIN,
    
    /**
     * Logout utilisateur
     */
    LOGOUT,
    
    /**
     * Accès refusé
     */
    ACCESS_DENIED,
    
    /**
     * Export de données
     */
    EXPORT,
    
    /**
     * Import de données
     */
    IMPORT
}
