package com.connect.Team.controller;

import com.connect.Team.dto.TeamResponse;
import com.connect.Team.service.TeamService;
import com.connect.Team.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for team management (read-only for admin)
 */
@RestController
@RequestMapping("/api/admin/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Admin read-only APIs for viewing teams and members")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {
    
    private final TeamService teamService;
    private final JwtUtil jwtUtil;
    
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
     * Get all teams in organization (including inactive)
     * GET /api/admin/teams
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all teams", description = "Retrieves all teams in the admin's organization with their members.")
    public ResponseEntity<List<TeamResponse>> getTeams(HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            List<TeamResponse> teams = teamService.getTeamsByOrganization(organizationId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get team by ID with members
     * GET /api/admin/teams/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get team", description = "Retrieves team details with all members.")
    public ResponseEntity<TeamResponse> getTeam(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            TeamResponse team = teamService.getTeamById(id, organizationId);
            return ResponseEntity.ok(team);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

