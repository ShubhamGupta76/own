package com.connect.User.service;

import com.connect.User.dto.EmployeeLoginRequest;
import com.connect.User.dto.EmployeeProfileResponse;
import com.connect.User.dto.EmployeeValidationResponse;
import com.connect.User.entity.User;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for employee operations
 * Handles employee login validation and profile management
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Validate employee credentials for login
     * Called by Auth Service
     */
    @Transactional(readOnly = true)
    public EmployeeValidationResponse validateEmployeeCredentials(EmployeeLoginRequest request) {
        // Find employee by email and role
        User user = userRepository.findByEmailAndRole(request.getEmail(), User.Role.EMPLOYEE)
                .orElse(null);
        
        // If not found, return invalid credentials
        if (user == null) {
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid email or password")
                    .build();
        }
        
        // Verify it's an employee
        if (user.getRole() != User.Role.EMPLOYEE) {
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid credentials")
                    .build();
        }
        
        // Verify user is active
        if (!user.getActive()) {
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Account is disabled")
                    .build();
        }
        
        // Verify password
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid email or password")
                    .build();
        }
        
        // CRITICAL: EMPLOYEE users MUST have organizationId assigned
        if (user.getOrganizationId() == null || user.getOrganizationId() == 0) {
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Employee account is not assigned to an organization. Please contact your administrator.")
                    .build();
        }
        
        return EmployeeValidationResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())
                .role(user.getRole())
                .isValid(true)
                .isFirstLogin(user.getIsFirstLogin())
                .message("Credentials valid")
                .build();
    }
    
    /**
     * Get employee profile by ID
     * Employee can only access their own profile
     */
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getEmployeeProfile(Long userId, Long requestingUserId) {
        // Verify employee is accessing their own profile
        if (!userId.equals(requestingUserId)) {
            throw new RuntimeException("Access denied: You can only access your own profile");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify it's an employee
        if (user.getRole() != User.Role.EMPLOYEE) {
            throw new RuntimeException("Access denied: Not an employee");
        }
        
        return mapToProfileResponse(user);
    }
    
    /**
     * Create or update employee profile on first login
     */
    @Transactional
    public EmployeeProfileResponse createOrUpdateEmployeeProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify it's an employee
        if (user.getRole() != User.Role.EMPLOYEE) {
            throw new RuntimeException("Access denied: Not an employee");
        }
        
        // If first login, set up profile
        if (user.getIsFirstLogin()) {
            // Generate display name
            if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
                user.setDisplayName(user.getFirstName() + " " + user.getLastName());
            }
            
            // Mark as not first login
            user.setIsFirstLogin(false);
        }
        
        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        
        // Ensure user is active
        user.setActive(true);
        
        user = userRepository.save(user);
        
        return mapToProfileResponse(user);
    }
    
    /**
     * Map User entity to EmployeeProfileResponse
     */
    private EmployeeProfileResponse mapToProfileResponse(User user) {
        return EmployeeProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .organizationId(user.getOrganizationId())
                .role(user.getRole().name())
                .active(user.getActive())
                .isFirstLogin(user.getIsFirstLogin())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

