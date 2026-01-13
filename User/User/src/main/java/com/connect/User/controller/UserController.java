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
@RequestMapping("/api/users")
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
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user in the admin's organization. Role can be ADMIN, MANAGER, or EMPLOYEE.")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found. Please create an organization first.");
            }
            
            User user = userService.createUser(request, organizationId, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get all users in organization
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users in the admin's organization.")
    public ResponseEntity<List<User>> getUsers(HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found. Please create an organization first.");
            }
            
            List<User> users = userService.getUsersByOrganization(organizationId, adminId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
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
            Long adminId = getAdminId(httpRequest);
            log.debug("Getting user {} by admin {}", id, adminId);
            User user = userService.getUserById(id, adminId);
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
}

