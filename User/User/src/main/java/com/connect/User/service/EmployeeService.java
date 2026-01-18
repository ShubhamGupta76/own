package com.connect.User.service;

import com.connect.User.dto.EmployeeLoginRequest;
import com.connect.User.dto.EmployeeProfileResponse;
import com.connect.User.dto.EmployeeValidationResponse;
import com.connect.User.entity.User;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EmployeeService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Validate employee credentials for login
     * Called by Auth Service
     */
    @Transactional(readOnly = true)
    public EmployeeValidationResponse validateEmployeeCredentials(EmployeeLoginRequest request) {
        log.info("Validating employee credentials for email: {}", request.getEmail());
        
        // Find employee by email and role
        // Note: Email lookup is case-sensitive by default in JPA
        // If exact match fails, we'll log it for debugging
        User user = userRepository.findByEmailAndRole(request.getEmail(), User.Role.EMPLOYEE)
                .orElse(null);
        
        // If not found with exact match, try case-insensitive search
        if (user == null) {
            log.debug("Exact email match not found for: {}, trying case-insensitive search", request.getEmail());
            // Search all users and filter by case-insensitive email and EMPLOYEE role
            user = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.EMPLOYEE && u.getEmail() != null)
                    .filter(u -> u.getEmail().equalsIgnoreCase(request.getEmail()))
                    .findFirst()
                    .orElse(null);
            
            if (user != null) {
                log.info("Found user with case-insensitive email match: stored={}, requested={}", 
                        user.getEmail(), request.getEmail());
            }
        }
        
        // If not found, return invalid credentials
        if (user == null) {
            log.warn("Employee not found with email: {} and role: EMPLOYEE", request.getEmail());
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid email or password. Please check your credentials or contact your administrator if you believe this is an error.")
                    .build();
        }
        
        log.debug("Found employee user: ID={}, Email={}, Role={}, Active={}, OrganizationId={}, HasPassword={}", 
                user.getId(), user.getEmail(), user.getRole(), user.getActive(), 
                user.getOrganizationId(), user.getPassword() != null && !user.getPassword().isEmpty());
        
        // Verify it's an employee
        if (user.getRole() != User.Role.EMPLOYEE) {
            log.warn("User found but role is not EMPLOYEE. User ID: {}, Role: {}", user.getId(), user.getRole());
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid credentials. This account is not an employee account.")
                    .build();
        }
        
        // Verify user is active
        if (!user.getActive()) {
            log.warn("Employee account is disabled. User ID: {}, Email: {}", user.getId(), user.getEmail());
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Account is disabled. Please contact your administrator.")
                    .build();
        }
        
        // CRITICAL: EMPLOYEE users MUST have organizationId assigned
        if (user.getOrganizationId() == null || user.getOrganizationId() == 0) {
            log.warn("Employee account has no organizationId. User ID: {}, Email: {}", user.getId(), user.getEmail());
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Employee account is not assigned to an organization. Please contact your administrator.")
                    .build();
        }
        
        // Verify password - check if password is set first
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.warn("Employee password not set. User ID: {}, Email: {}", user.getId(), user.getEmail());
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Password not set for this account. Please contact your administrator to set your password.")
                    .build();
        }
        
        // Verify password matches
        log.debug("Attempting password match for user {}: Email={}, Stored password length={}, Input password length={}", 
                user.getId(), user.getEmail(), 
                user.getPassword() != null ? user.getPassword().length() : 0,
                request.getPassword() != null ? request.getPassword().length() : 0);
        
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("Password match result for user {} ({}): {}", user.getId(), user.getEmail(), passwordMatches);
        
        if (!passwordMatches) {
            log.warn("Password mismatch for employee. User ID: {}, Email: {}, Stored password hash: {}...", 
                    user.getId(), user.getEmail(), 
                    user.getPassword() != null && user.getPassword().length() > 10 
                        ? user.getPassword().substring(0, 10) : "null");
            return EmployeeValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid email or password. Please check your credentials.")
                    .build();
        }
        
        log.info("Employee credentials validated successfully. User ID: {}, Email: {}", user.getId(), user.getEmail());
        
        return EmployeeValidationResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())
                .role(user.getRole().name()) // Convert enum to string
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

