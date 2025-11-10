package com.supplychainx.audit.enums;

/**
 * Types d'alertes possibles dans le système
 */
public enum AlertType {
    /**
     * Alerte pour stock faible (matière première ou produit)
     */
    LOW_STOCK,
    
    /**
     * Alerte pour rupture de stock
     */
    OUT_OF_STOCK,
    
    /**
     * Alerte pour stock critique
     */
    CRITICAL_STOCK,
    
    /**
     * Alerte pour retard de livraison
     */
    DELIVERY_DELAY,
    
    /**
     * Alerte pour commande bloquée
     */
    ORDER_BLOCKED,
    
    /**
     * Alerte générale système
     */
    SYSTEM_ALERT
}
