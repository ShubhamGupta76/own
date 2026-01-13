package com.connect.User.service;

import com.connect.User.dto.UserRequest;
import com.connect.User.entity.Organization;
import com.connect.User.entity.User;
import com.connect.User.repository.OrganizationRepository;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for user management
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    
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
        
        // Set password for EMPLOYEE role users
        if (role == User.Role.EMPLOYEE && request.getPassword() != null && !request.getPassword().isEmpty()) {
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
     * Get user by ID (admin can only access users in their own organization)
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify admin owns the organization
        Organization organization = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access users in your own organization");
        }
        
        return user;
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

