package com.connect.User.controller;

import com.connect.User.dto.OrganizationRequest;
import com.connect.User.dto.OrganizationResponse;
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

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Admin APIs for organization management")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    private final JwtUtil jwtUtil;
    
    private Long getAdminId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    @PostMapping(value = {"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create organization", description = "Creates a new organization (tenant/company) and returns a new JWT token with organizationId. Admin must be authenticated.")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = getAdminId(httpRequest);
        
        // Check if admin already has an organization
        try {
            organizationService.getOrganizationByAdminId(adminId);
            // Admin already has an organization, return conflict
            throw new RuntimeException("Organization already exists for this admin. Please use the existing organization.");
        } catch (RuntimeException e) {
            // If exception message contains "not found", admin doesn't have org yet, continue
            if (e.getMessage() != null && e.getMessage().contains("Organization not found")) {
                // Admin doesn't have organization, proceed with creation
                // The service will throw if organization name already exists globally
                OrganizationResponse response = organizationService.createOrganizationWithToken(request, adminId);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            // Re-throw other exceptions (like "already exists for this admin")
            throw e;
        }
    }
    
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
    
    @GetMapping("/my-organization")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get my organization", description = "Retrieves the organization associated with the authenticated admin. Returns 404 if no organization exists yet.")
    public ResponseEntity<Organization> getMyOrganization(HttpServletRequest httpRequest) {
        try {
            Long adminId = getAdminId(httpRequest);
            Organization organization = organizationService.getOrganizationByAdminId(adminId);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            // Let GlobalExceptionHandler convert to 404
            throw e;
        }
    }

    @GetMapping("/admin/{adminId}")
    @Operation(summary = "Get organization by admin ID (internal)", description = "Internal endpoint for migration. Returns organization for the given admin ID.")
    public ResponseEntity<Organization> getOrganizationByAdminIdInternal(@PathVariable Long adminId) {
        try {
            Organization organization = organizationService.getOrganizationByAdminId(adminId);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

