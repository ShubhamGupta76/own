package com.connect.Team.controller;

import com.connect.Team.dto.AddTeamMemberRequest;
import com.connect.Team.dto.CreateTeamRequest;
import com.connect.Team.dto.TeamMemberResponse;
import com.connect.Team.dto.TeamResponse;
import com.connect.Team.service.TeamManagementService;
import com.connect.Team.util.JwtUtil;
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
 * Controller for team management operations
 * ADMIN and MANAGER can create/manage teams
 * All roles can view teams
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Team creation and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TeamManagementController {
    
    private final TeamManagementService teamManagementService;
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
     * Create a new team
     * POST /api/teams
     * ADMIN and MANAGER only
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create team", description = "Creates a new team. Only ADMIN and MANAGER can create teams. Auto-creates 'General' channel.")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TeamResponse team = teamManagementService.createTeam(request, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(team);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Add member to team
     * POST /api/teams/{teamId}/members
     * ADMIN and MANAGER only
     */
    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Add team member", description = "Adds a user to a team. Only ADMIN and MANAGER can add members.")
    public ResponseEntity<TeamMemberResponse> addTeamMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request,
            HttpServletRequest httpRequest) {
        try {
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TeamMemberResponse member = teamManagementService.addTeamMember(teamId, request, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(member);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Remove member from team
     * DELETE /api/teams/{teamId}/members/{userId}
     * ADMIN and MANAGER only
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Remove team member", description = "Removes a user from a team. Only ADMIN and MANAGER can remove members. Cannot remove team owner.")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        try {
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            teamManagementService.removeTeamMember(teamId, userId, organizationId, role);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get all teams in organization
     * GET /api/teams
     * All roles can view
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all teams", description = "Retrieves all active teams in the organization. All roles can view.")
    public ResponseEntity<List<TeamResponse>> getAllTeams(HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<TeamResponse> teams = teamManagementService.getAllTeams(organizationId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get teams for logged-in user
     * GET /api/teams/my
     * All roles can view their own teams
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get my teams", description = "Retrieves teams where the logged-in user is a member. All roles can view.")
    public ResponseEntity<List<TeamResponse>> getMyTeams(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<TeamResponse> teams = teamManagementService.getMyTeams(userId, organizationId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

