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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Team Management", description = "Team creation and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TeamManagementController {
    
    private final TeamManagementService teamManagementService;
    private final JwtUtil jwtUtil;
    
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private String getRole(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String authorityName = authority.getAuthority();
                if (authorityName.startsWith("ROLE_")) {
                    String role = authorityName.substring(5);
                    log.debug("Extracted role from SecurityContext: {}", role);
                    return role;
                }
            }
        }
        
        String token = extractToken(request);
        String role = jwtUtil.extractRole(token);
        log.debug("Extracted role from token: {}", role);
        return role;
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
            
            log.debug("Creating team - UserId: {}, Role: {}, OrganizationId: {}", userId, role, organizationId);
            
            if (role == null || role.trim().isEmpty()) {
                log.error("Role is null or empty when creating team. UserId: {}, OrganizationId: {}", userId, organizationId);
                throw new RuntimeException("User role is missing from token");
            }
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            TeamResponse team = teamManagementService.createTeam(request, userId, organizationId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(team);
        } catch (RuntimeException e) {
            log.error("Error creating team: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
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

