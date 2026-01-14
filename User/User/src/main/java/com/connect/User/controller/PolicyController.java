package com.connect.User.controller;

import com.connect.User.dto.PolicyRequest;
import com.connect.User.entity.Policy;
import com.connect.User.service.PolicyService;
import com.connect.User.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for policy management
 * ADMIN only endpoints
 */
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "Admin APIs for organization-level policy management")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {
    
    private final PolicyService policyService;
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
     * Get all policies for organization
     * GET /api/policies
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all policies", description = "Retrieves all policies (CHAT, MEETING, FILE_SHARING) for the admin's organization.")
    public ResponseEntity<List<Policy>> getPolicies(HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found. Please create an organization first.");
            }
            
            List<Policy> policies = policyService.getPoliciesByOrganization(organizationId, adminId);
            return ResponseEntity.ok(policies);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get specific policy
     * GET /api/policies/{policyType}
     */
    @GetMapping("/{policyType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get policy", description = "Retrieves a specific policy (CHAT, MEETING, FILE_SHARING) for the admin's organization.")
    public ResponseEntity<Policy> getPolicy(
            @PathVariable String policyType,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found. Please create an organization first.");
            }
            
            Policy.PolicyType type;
            try {
                type = Policy.PolicyType.valueOf(policyType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid policy type: " + policyType);
            }
            
            Policy policy = policyService.getPolicy(organizationId, type, adminId);
            return ResponseEntity.ok(policy);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Update policy (enable/disable)
     * PUT /api/policies/{policyType}
     */
    @PutMapping("/{policyType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update policy", description = "Enables or disables a specific policy (CHAT, MEETING, FILE_SHARING) for the admin's organization.")
    public ResponseEntity<Policy> updatePolicy(
            @PathVariable String policyType,
            @Valid @RequestBody PolicyRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found. Please create an organization first.");
            }
            
            // Ensure policy type matches
            if (!policyType.equalsIgnoreCase(request.getPolicyType())) {
                throw new RuntimeException("Policy type mismatch");
            }
            
            Policy policy = policyService.updatePolicy(organizationId, request, adminId);
            return ResponseEntity.ok(policy);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

