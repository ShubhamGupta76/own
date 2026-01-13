package com.connect.User.controller;

import com.connect.User.dto.OrganizationRequest;
import com.connect.User.entity.Organization;
import com.connect.User.service.OrganizationService;
import com.connect.User.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for organization management
 * ADMIN only endpoints
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Admin APIs for organization management")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract admin ID from JWT token
     */
    private Long getAdminId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
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
     * Create a new organization
     * POST /api/organizations
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create organization", description = "Creates a new organization (tenant/company). Admin must be authenticated.")
    public ResponseEntity<Organization> createOrganization(
            @Valid @RequestBody OrganizationRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Organization organization = organizationService.createOrganization(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(organization);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get organization by ID
     * GET /api/organizations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get organization", description = "Retrieves organization details. Admin can only access their own organization.")
    public ResponseEntity<Organization> getOrganization(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Organization organization = organizationService.getOrganization(id, adminId);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get organization by admin ID
     * GET /api/organizations/my-organization
     */
    @GetMapping("/my-organization")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get my organization", description = "Retrieves the organization associated with the authenticated admin.")
    public ResponseEntity<Organization> getMyOrganization(HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Organization organization = organizationService.getOrganizationByAdminId(adminId);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

