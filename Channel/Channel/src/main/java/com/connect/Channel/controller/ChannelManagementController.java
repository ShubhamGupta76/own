package com.connect.Channel.controller;

import com.connect.Channel.dto.ChannelMemberResponse;
import com.connect.Channel.dto.ChannelResponse;
import com.connect.Channel.dto.CreateChannelRequest;
import com.connect.Channel.service.ChannelManagementService;
import com.connect.Channel.util.JwtUtil;
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

import java.util.List;

/**
 * Controller for channel management operations
 * ADMIN and MANAGER: full access
 * EMPLOYEE: can create channels if team member
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Channel Management", description = "Channel creation and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ChannelManagementController {
    
    private final ChannelManagementService channelManagementService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private String getRole(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractRole(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Create channel in a team
     * POST /api/teams/{teamId}/channels
     * ADMIN, MANAGER: full access
     * EMPLOYEE: only if team member
     */
    @PostMapping("/teams/{teamId}/channels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Create channel", description = "Creates a channel in a team. ADMIN and MANAGER have full access. EMPLOYEE can create only if they are a team member.")
    public ResponseEntity<ChannelResponse> createChannel(
            @PathVariable Long teamId,
            @Valid @RequestBody CreateChannelRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            ChannelResponse channel = channelManagementService.createChannel(teamId, request, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(channel);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get channels under a team
     * GET /api/teams/{teamId}/channels
     * All roles can view
     */
    @GetMapping("/teams/{teamId}/channels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get team channels", description = "Retrieves all channels under a team. All roles can view.")
    public ResponseEntity<List<ChannelResponse>> getChannelsByTeam(
            @PathVariable Long teamId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<ChannelResponse> channels = channelManagementService.getChannelsByTeam(teamId, organizationId);
            return ResponseEntity.ok(channels);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Add member to channel
     * POST /api/channels/{channelId}/members
     * ADMIN, MANAGER: full access
     * EMPLOYEE: only if team member
     */
    @PostMapping("/channels/{channelId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Add channel member", description = "Adds a user to a channel. ADMIN and MANAGER have full access. EMPLOYEE can add only if they are a team member.")
    public ResponseEntity<ChannelMemberResponse> addChannelMember(
            @PathVariable Long channelId,
            @RequestBody java.util.Map<String, Long> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            Long memberUserId = request.get("userId");
            
            if (memberUserId == null) {
                throw new RuntimeException("User ID is required");
            }
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            ChannelMemberResponse member = channelManagementService.addChannelMember(
                    channelId, memberUserId, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(member);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Remove member from channel
     * DELETE /api/channels/{channelId}/members/{userId}
     * ADMIN, MANAGER: full access
     * EMPLOYEE: only if team member
     */
    @DeleteMapping("/channels/{channelId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Remove channel member", description = "Removes a user from a channel. ADMIN and MANAGER have full access. EMPLOYEE can remove only if they are a team member.")
    public ResponseEntity<Void> removeChannelMember(
            @PathVariable Long channelId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            channelManagementService.removeChannelMember(channelId, userId, currentUserId, organizationId, role);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get channel members
     * GET /api/channels/{channelId}/members
     * All roles can view
     */
    @GetMapping("/channels/{channelId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get channel members", description = "Retrieves all members of a channel. All roles can view.")
    public ResponseEntity<List<ChannelMemberResponse>> getChannelMembers(
            @PathVariable Long channelId,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<ChannelMemberResponse> members = channelManagementService.getChannelMembers(channelId, organizationId);
            return ResponseEntity.ok(members);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

