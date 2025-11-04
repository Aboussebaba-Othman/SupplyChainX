package com.supplychainx.security.service;

import com.supplychainx.common.enums.Role;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.security.dto.request.ChangePasswordRequestDTO;
import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
import com.supplychainx.security.entity.User;
import com.supplychainx.security.mapper.UserMapper;
import com.supplychainx.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Load user by username for Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Create a new user
     */
    public UserResponseDTO createUser(UserRequestDTO dto) {
        log.info("Creating new user: {}", dto.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        // Map DTO to entity
        User user = userMapper.toEntity(dto);
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Set default values
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setFailedLoginAttempts(0);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully - ID: {}, Username: {}", savedUser.getId(), savedUser.getUsername());

        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponseDTO(user);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userMapper.toResponseDTO(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        log.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String search) {
        log.debug("Searching users with term: {}", search);
        return userRepository.searchUsers(search).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user information
     */
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        log.info("Updating user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if new username is taken by another user
        if (!user.getUsername().equals(dto.getUsername()) && 
            userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + dto.getUsername());
        }

        // Check if new email is taken by another user
        if (!user.getEmail().equals(dto.getEmail()) && 
            userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        // Update fields (password and role are updated separately)
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully - ID: {}", id);

        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(Long id, ChangePasswordRequestDTO dto) {
        log.info("Changing password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Verify new password and confirmation match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("New password and confirmation do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", id);
    }

    /**
     * Change user role (admin only)
     */
    public UserResponseDTO changeRole(Long id, Role newRole) {
        log.info("Changing role for user ID: {} to {}", id, newRole);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        log.info("Role changed successfully for user ID: {}", id);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Enable user account
     */
    public UserResponseDTO enableUser(Long id) {
        log.info("Enabling user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setEnabled(true);
        User updatedUser = userRepository.save(user);

        log.info("User enabled successfully - ID: {}", id);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Disable user account
     */
    public UserResponseDTO disableUser(Long id) {
        log.info("Disabling user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setEnabled(false);
        User updatedUser = userRepository.save(user);

        log.info("User disabled successfully - ID: {}", id);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Lock user account
     */
    public UserResponseDTO lockUser(Long id) {
        log.info("Locking user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.lock();
        User updatedUser = userRepository.save(user);

        log.info("User locked successfully - ID: {}", id);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Unlock user account
     */
    public UserResponseDTO unlockUser(Long id) {
        log.info("Unlocking user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.unlock();
        User updatedUser = userRepository.save(user);

        log.info("User unlocked successfully - ID: {}", id);
        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully - ID: {}", id);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin(String username) {
        log.debug("Updating last login for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        user.updateLastLogin();
        userRepository.save(user);
    }

    /**
     * Handle failed login attempt
     */
    public void handleFailedLogin(String username) {
        log.warn("Failed login attempt for user: {}", username);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            
            if (user.getFailedLoginAttempts() >= 5) {
                log.warn("User account temporarily locked due to failed login attempts: {}", username);
            }
        });
    }

    /**
     * Get user entity by username (for internal use)
     */
    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
