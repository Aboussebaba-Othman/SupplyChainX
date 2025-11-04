package com.supplychainx.security.config;

import com.supplychainx.common.enums.Role;
import com.supplychainx.security.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Expressions de sécurité personnalisées pour le contrôle d'accès par module
 * Utilisées avec @PreAuthorize("@securityExpressions.hasSupplyAccess()")
 * 
 * @author SupplyChainX Team
 * @version 1.1.0
 */
@Component("securityExpressions")
public class SecurityExpressions {
    
    /**
     * Vérifie si l'utilisateur connecté a accès au module Supply
     * 
     * @return true si l'utilisateur a accès au module Supply
     */
    public boolean hasSupplyAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            User user = (User) principal;
            return user.getRole().canAccessSupplyModule();
        }
        
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur connecté a accès au module Production
     * 
     * @return true si l'utilisateur a accès au module Production
     */
    public boolean hasProductionAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            User user = (User) principal;
            return user.getRole().canAccessProductionModule();
        }
        
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur connecté a accès au module Delivery
     * 
     * @return true si l'utilisateur a accès au module Delivery
     */
    public boolean hasDeliveryAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            User user = (User) principal;
            return user.getRole().canAccessDeliveryModule();
        }
        
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur connecté est un administrateur
     * 
     * @return true si l'utilisateur est admin
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            User user = (User) principal;
            return user.getRole() == Role.ADMIN;
        }
        
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur peut gérer les utilisateurs (admin uniquement)
     * 
     * @return true si l'utilisateur peut gérer les utilisateurs
     */
    public boolean canManageUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            User user = (User) principal;
            return user.getRole().canManageUsers();
        }
        
        return false;
    }
}
