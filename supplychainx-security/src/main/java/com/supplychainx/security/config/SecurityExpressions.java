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
    

    public boolean hasAnyPermission(String... permissionNames) {
        return Arrays.stream(permissionNames)
                .anyMatch(this::hasPermission);
    }
    

    public boolean hasAllPermissions(String... permissionNames) {
        return Arrays.stream(permissionNames)
                .allMatch(this::hasPermission);
    }

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
