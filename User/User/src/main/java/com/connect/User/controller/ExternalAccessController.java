package com.connect.User.controller;

import com.connect.User.dto.ExternalAccessResponse;
import com.connect.User.dto.InviteExternalUserRequest;
import com.connect.User.service.ExternalAccessService;
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
 * Controller for external user access management
 * Handles inviting external users and managing their access
 */
@RestController
@RequestMapping("/api/v1/external")
@RequiredArgsConstructor
@Tag(name = "External Access", description = "External user invitation and access management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExternalAccessController {
    
    private final ExternalAccessService externalAccessService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    private String getRole(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractRole(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Invite an external user
     * POST /api/external/invite
     * Only ADMIN can invite external users
     */
    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Invite external user", description = "Invites an external user (vendor/client) and grants access to specific teams/channels. Only ADMIN can invite.")
    public ResponseEntity<ExternalAccessResponse> inviteExternalUser(
            @Valid @RequestBody InviteExternalUserRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            // Validate that at least one team or channel is provided
            if ((request.getTeamIds() == null || request.getTeamIds().isEmpty()) &&
                (request.getChannelIds() == null || request.getChannelIds().isEmpty())) {
                throw new RuntimeException("At least one team or channel must be specified for external access");
            }
            
            ExternalAccessResponse response = externalAccessService.inviteExternalUser(request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get external access for a user
     * GET /api/external/access
     */
    @GetMapping("/access")
    @PreAuthorize("hasAnyRole('ADMIN','EXTERNAL_USER')")
    @Operation(summary = "Get external access", description = "Retrieves external access mappings for a user. ADMIN can view any user, EXTERNAL_USER can only view their own.")
    public ResponseEntity<ExternalAccessResponse> getExternalAccess(
            @RequestParam(required = false) Long userId,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            // If userId not provided, use current user
            // EXTERNAL_USER can only view their own access
            if (userId == null) {
                userId = currentUserId;
            } else if (role.equals("EXTERNAL_USER") && !userId.equals(currentUserId)) {
                throw new RuntimeException("Access denied: You can only view your own access");
            }
            
            ExternalAccessResponse response = externalAccessService.getExternalAccess(userId, organizationId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

