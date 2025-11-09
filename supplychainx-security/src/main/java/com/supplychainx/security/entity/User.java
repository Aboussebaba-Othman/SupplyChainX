package com.supplychainx.security.entity;

import com.supplychainx.common.entity.BaseEntity;
import com.supplychainx.common.enums.Permission;
import com.supplychainx.common.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 60, max = 100, message = "Password must be encrypted (BCrypt)")
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(name = "account_non_expired", nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired", nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // Spring Security UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Add role authority (ROLE_*)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        
        // Add all permissions for this role (PERM_*)
        for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getAuthority()));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Check if account is temporarily locked
        if (!accountNonLocked) {
            return false;
        }
        if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Business methods

    /**
     * Get user's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Check if user can access Supply module
     */
    public boolean canAccessSupplyModule() {
        return role.canAccessSupplyModule();
    }

    /**
     * Check if user can access Production module
     */
    public boolean canAccessProductionModule() {
        return role.canAccessProductionModule();
    }

    /**
     * Check if user can access Delivery module
     */
    public boolean canAccessDeliveryModule() {
        return role.canAccessDeliveryModule();
    }

    /**
     * Check if user can manage other users
     */
    public boolean canManageUsers() {
        return role.canManageUsers();
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Increment failed login attempts and lock account if necessary
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            // Lock account for 15 minutes after 5 failed attempts
            this.lockedUntil = LocalDateTime.now().plusMinutes(15);
        }
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Unlock account
     */
    public void unlock() {
        this.accountNonLocked = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Lock account permanently
     */
    public void lock() {
        this.accountNonLocked = false;
    }
}
