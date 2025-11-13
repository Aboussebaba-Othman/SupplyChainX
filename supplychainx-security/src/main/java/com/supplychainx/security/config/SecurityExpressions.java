package com.supplychainx.security.config;

import com.supplychainx.common.enums.Role;
import com.supplychainx.security.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component("securityExpressions")
public class SecurityExpressions {
    

    public boolean hasPermission(String permissionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String authority = "PERM_" + permissionName;
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authority));
    }
    
    /**
     * Vérifie si l'utilisateur a au moins une des permissions spécifiées
     * Usage: @PreAuthorize("@securityExpressions.hasAnyPermission('SUPPLIER_CREATE', 'SUPPLIER_UPDATE')")
     * 
     * @param permissionNames les noms des permissions
     * @return true si l'utilisateur a au moins une des permissions
     */
    public boolean hasAnyPermission(String... permissionNames) {
        return Arrays.stream(permissionNames)
                .anyMatch(this::hasPermission);
    }
    
    /**
     * Vérifie si l'utilisateur a toutes les permissions spécifiées
     * Usage: @PreAuthorize("@securityExpressions.hasAllPermissions('SUPPLIER_READ', 'SUPPLIER_UPDATE')")
     * 
     * @param permissionNames les noms des permissions
     * @return true si l'utilisateur a toutes les permissions
     */
    public boolean hasAllPermissions(String... permissionNames) {
        return Arrays.stream(permissionNames)
                .allMatch(this::hasPermission);
    }
    
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
