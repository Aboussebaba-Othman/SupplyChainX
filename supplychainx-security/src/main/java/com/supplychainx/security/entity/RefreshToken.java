package com.supplychainx.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Refresh token entity for JWT token rotation and revocation
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique refresh token string
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    // Associated user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Token expiration date
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Revocation flag (true = token invalidated)
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    // Creation timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Last usage timestamp
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Check if token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Check if token is valid (not expired and not revoked)
    public boolean isValid() {
        return !isExpired() && !revoked;
    }
}
