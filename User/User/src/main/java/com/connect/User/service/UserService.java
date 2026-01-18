package com.connect.User.service;

import com.connect.User.dto.UserRequest;
import com.connect.User.entity.Organization;
import com.connect.User.entity.User;
import com.connect.User.repository.OrganizationRepository;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Get organization ID by admin ID (fallback when organizationId is not in token)
     */
    @Transactional(readOnly = true)
    public Long getOrganizationIdByAdminId(Long adminId) {
        return organizationRepository.findByAdminId(adminId)
                .map(Organization::getId)
                .orElse(null);
    }
    
    /**
     * Create a new user in an organization
     */
    @Transactional
    public User createUser(UserRequest request, Long organizationId, Long adminId) {
        // Verify organization exists and admin owns it
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only manage users in your own organization");
        }
        
        // Check if user already exists in this organization
        if (userRepository.existsByEmailAndOrganizationId(request.getEmail(), organizationId)) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists in this organization");
        }
        
        // Validate role
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .organizationId(organizationId)
                .role(role)
                .active(true)
                .isFirstLogin(true) // New users start with first login flag
                .build();
        
        // Set password for EMPLOYEE role users - password is REQUIRED for EMPLOYEE
        if (role == User.Role.EMPLOYEE) {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new RuntimeException("Password is required when creating an EMPLOYEE user");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Get all users in an organization (admin can only access their own organization)
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByOrganization(Long organizationId, Long adminId) {
        // Verify organization exists and admin owns it
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access users in your own organization");
        }
        
        return userRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Get all users in an organization (for members - no admin validation)
     * Used for adding members to channels/teams
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByOrganizationForMembers(Long organizationId) {
        // Verify organization exists
        if (!organizationRepository.existsById(organizationId)) {
            throw new RuntimeException("Organization not found");
        }
        
        return userRepository.findByOrganizationIdAndActiveTrue(organizationId);
    }
    
    /**
     * Get user by ID (admin can only access users in their own organization)
     * If userId matches adminId, find user by organization's admin relationship
     * Uses email to find the correct admin user when multiple admins exist
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId, Long adminId, String email, Long organizationId) {
        // If admin is accessing their own profile (userId == adminId)
        // Try to find user by adminId first, then by email and organization
        if (userId.equals(adminId)) {
            // If we have email and organizationId, prioritize finding by email
            // This ensures we get the correct admin user when multiple admins exist
            if (email != null && !email.isEmpty() && organizationId != null && organizationId > 0) {
                User userByEmail = userRepository.findByEmailAndOrganizationId(email, organizationId)
                        .orElse(null);
                if (userByEmail != null) {
                    return userByEmail;
                }
            }
            
            // Try to find by ID (in case User ID matches Admin ID)
            // But verify email matches if email is provided
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                // If email is provided, verify it matches
                if (email != null && !email.isEmpty() && !email.equalsIgnoreCase(user.getEmail())) {
                    // Email doesn't match, continue searching
                } else {
                    return user;
                }
            }
            
            // If not found by email, find by organization's admin relationship
            // This handles the case where Admin ID (from Auth service) doesn't match User ID
            Organization organization = organizationRepository.findByAdminId(adminId)
                    .orElse(null);
            if (organization != null) {
                // If we have email, try to find by email and organization first
                if (email != null && !email.isEmpty()) {
                    User userByEmail = userRepository.findByEmailAndOrganizationId(email, organization.getId())
                            .orElse(null);
                    if (userByEmail != null) {
                        return userByEmail;
                    }
                }
                
                // Fallback: Find the ADMIN user in this organization (should be the admin's User record)
                User adminUser = userRepository.findByOrganizationIdAndRole(organization.getId(), User.Role.ADMIN)
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (adminUser != null) {
                    return adminUser;
                }
            }
            
            // If still not found, throw error
            throw new RuntimeException("User not found. Admin user record may not have been created.");
        }
        
        // Standard lookup for other users
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // If user has no organizationId, deny access
        if (user.getOrganizationId() == null) {
            throw new RuntimeException("User does not belong to any organization");
        }
        
        // Verify admin owns the organization
        Organization organization = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access users in your own organization");
        }
        
        return user;
    }
    
    /**
     * Get user by ID (backward compatibility - uses email from token if available)
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId, Long adminId) {
        return getUserById(userId, adminId, null, null);
    }
    
    /**
     * Update user password
     * Admin can reset passwords for any user in their organization
     */
    @Transactional
    public User updateUserPassword(Long userId, String newPassword, Long adminId) {
        // Get user and verify admin has access (without readOnly to allow modification)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify admin has access to this user's organization
        if (user.getOrganizationId() == null) {
            throw new RuntimeException("User does not belong to any organization");
        }
        
        Organization organization = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only reset passwords for users in your own organization");
        }
        
        log.info("Updating password for user ID: {}, Email: {}, Role: {}", userId, user.getEmail(), user.getRole());
        log.debug("New password length: {}", newPassword.length());
        
        // Encode and set new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        log.debug("Password encoded. Encoded length: {}", encodedPassword.length());
        user.setPassword(encodedPassword);
        
        // Save the user - @Transactional ensures it's committed
        User savedUser = userRepository.save(user);
        
        log.info("Password updated successfully. User ID: {}, Email: {}, Has password: {}, Password hash prefix: {}", 
                savedUser.getId(), savedUser.getEmail(), 
                savedUser.getPassword() != null && !savedUser.getPassword().isEmpty(),
                savedUser.getPassword() != null && savedUser.getPassword().length() > 10 
                    ? savedUser.getPassword().substring(0, 10) + "..." : "null");
        
        // Test password match immediately to verify encoding works
        boolean testMatch = passwordEncoder.matches(newPassword, savedUser.getPassword());
        log.info("Immediate password verification test after save: {}", testMatch);
        
        if (!testMatch) {
            log.error("CRITICAL: Password verification failed immediately after save! This indicates an encoding issue. User ID: {}", userId);
            throw new RuntimeException("Password encoding verification failed. Please contact support.");
        }
        
        return savedUser;
    }
    
    /**
     * Update user role
     */
    @Transactional
    public User updateUserRole(Long userId, String role, Long adminId) {
        User user = getUserById(userId, adminId);
        
        // Validate role
        User.Role newRole;
        try {
            newRole = User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
        
        user.setRole(newRole);
        return userRepository.save(user);
    }
    
    /**
     * Enable/disable user
     */
    @Transactional
    public User updateUserStatus(Long userId, Boolean active, Long adminId) {
        User user = getUserById(userId, adminId);
        user.setActive(active);
        return userRepository.save(user);
    }
}

