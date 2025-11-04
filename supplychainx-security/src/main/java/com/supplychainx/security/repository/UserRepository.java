package com.supplychainx.security.repository;

import com.supplychainx.common.enums.Role;
import com.supplychainx.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users by role
     */
    List<User> findByRole(Role role);

    /**
     * Find all enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find all locked users
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false OR u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role and enabled status
     */
    List<User> findByRoleAndEnabled(Role role, Boolean enabled);

    /**
     * Count users by role
     */
    long countByRole(Role role);

    /**
     * Search users by username, email, first name or last name
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);
}
