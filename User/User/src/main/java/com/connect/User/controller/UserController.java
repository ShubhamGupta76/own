package com.connect.User.controller;

import com.connect.User.dto.UserRequest;
import com.connect.User.entity.User;
import com.connect.User.service.UserService;
import com.connect.User.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for user management
 * ADMIN only endpoints
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Admin APIs for user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract admin ID and organization ID from JWT token
     */
    private Long getAdminId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    /**
     * Extract token from request
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Create a new user
     * POST /api/users or /api/users/
     */
    @PostMapping(value = {"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user in the admin's organization. Role can be ADMIN, MANAGER, or EMPLOYEE.")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            // If organizationId is not in token, try to get it from organization by adminId
            if (organizationId == null) {
                log.warn("OrganizationId not found in token for admin {}, attempting to get from organization", adminId);
                organizationId = userService.getOrganizationIdByAdminId(adminId);
                if (organizationId == null) {
                    throw new RuntimeException("Organization not found. Please create an organization first.");
                }
            }
            
            log.info("Creating user with email {} in organization {} by admin {}", 
                    request.getEmail(), organizationId, adminId);
            User user = userService.createUser(request, organizationId, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get all users in organization
     * GET /api/users or /api/users/
     */
    @GetMapping(value = {"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users in the admin's organization.")
    public ResponseEntity<List<User>> getUsers(HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            // If organizationId is not in token, try to get it from organization by adminId
            if (organizationId == null) {
                log.warn("OrganizationId not found in token for admin {}, attempting to get from organization", adminId);
                organizationId = userService.getOrganizationIdByAdminId(adminId);
                if (organizationId == null) {
                    throw new RuntimeException("Organization not found. Please create an organization first.");
                }
            }
            
            List<User> users = userService.getUsersByOrganization(organizationId, adminId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user", description = "Retrieves user details by ID.")
    public ResponseEntity<User> getUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            String token = extractToken(httpRequest);
            Long adminId = getAdminId(httpRequest);
            String email = jwtUtil.extractEmail(token);
            Long organizationId = getOrganizationId(httpRequest);
            
            log.debug("Getting user {} by admin {} (email: {}, orgId: {})", id, adminId, email, organizationId);
            User user = userService.getUserById(id, adminId, email, organizationId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error getting user {}: {}", id, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Update user role
     * PUT /api/users/{id}/role
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Updates the role of a user (ADMIN, MANAGER, EMPLOYEE).")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            String role = request.get("role");
            if (role == null) {
                throw new RuntimeException("Role is required");
            }
            User user = userService.updateUserRole(id, role, adminId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Enable/disable user
     * PUT /api/users/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Enables or disables a user account.")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Boolean active = request.get("active");
            if (active == null) {
                throw new RuntimeException("Active status is required");
            }
            User user = userService.updateUserStatus(id, active, adminId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Reset user password
     * PUT /api/users/{id}/password or /api/users/{id}/password/
     */
    @PutMapping(value = {"/{id}/password", "/{id}/password/"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset user password", description = "Resets the password for a user. Required for EMPLOYEE users who don't have a password set.")
    public ResponseEntity<User> resetUserPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Reset password request received for user ID: {}", id);
            Long adminId = getAdminId(httpRequest);
            log.debug("Admin ID: {}, Request body keys: {}", adminId, request.keySet());
            
            String newPassword = request.get("password");
            if (newPassword == null || newPassword.isEmpty()) {
                log.warn("Password is missing in request for user ID: {}", id);
                throw new RuntimeException("Password is required");
            }
            if (newPassword.length() < 6) {
                log.warn("Password too short for user ID: {} (length: {})", id, newPassword.length());
                throw new RuntimeException("Password must be at least 6 characters long");
            }
            
            log.info("Resetting password for user ID: {} by admin ID: {}", id, adminId);
            User user = userService.updateUserPassword(id, newPassword, adminId);
            log.info("Password reset successful for user ID: {}", id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error resetting password for user ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get organization members (for adding to channels/teams)
     * GET /api/users/organization/members
     * Accessible by ADMIN, MANAGER, and EMPLOYEE roles
     */
    @GetMapping("/organization/members")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get organization members", description = "Retrieves all users in the current user's organization. Used for adding members to channels and teams.")
    public ResponseEntity<List<User>> getOrganizationMembers(HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null || organizationId == 0) {
                throw new RuntimeException("Organization context is missing. Please log out and log back in to refresh your authentication token.");
            }
            
            // Get users by organization - allow any authenticated user in the organization
            List<User> users = userService.getUsersByOrganizationForMembers(organizationId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            log.error("Error getting organization members: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}

