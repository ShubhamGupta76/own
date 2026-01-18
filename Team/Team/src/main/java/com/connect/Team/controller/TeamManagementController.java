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
@RequestMapping("/api/v1/teams")
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
    
    @PostMapping(value = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create team", description = "Creates a new team. Only ADMIN and MANAGER can create teams. Auto-creates 'General' channel.")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Log authentication context for debugging
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                log.info("createTeam - Authentication: {}, Authorities: {}", auth.getName(), auth.getAuthorities());
            } else {
                log.warn("createTeam - No authentication found in SecurityContext");
            }
            
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            log.info("Creating team - UserId: {}, Role: {}, OrganizationId: {}", userId, role, organizationId);
            
            if (role == null || role.trim().isEmpty()) {
                log.error("Role is null or empty when creating team. UserId: {}, OrganizationId: {}", userId, organizationId);
                throw new RuntimeException("Access denied: User role is missing from token. Please log out and log back in.");
            }
            
            // Normalize role to uppercase for comparison
            String normalizedRole = role.trim().toUpperCase();
            if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("MANAGER")) {
                log.error("User with role '{}' attempted to create team. UserId: {}", role, userId);
                throw new RuntimeException("Access denied: Only ADMIN and MANAGER roles can create teams");
            }
            
            if (organizationId == null || organizationId == 0) {
                log.error("Missing organizationId for userId: {}, role: {} in createTeam. Token may not have organizationId set. User needs to log out and log back in to get a fresh token.", userId, role);
                throw new RuntimeException("Access denied: Organization context is missing. Your account may not be associated with an organization yet, or you're using an old token. Please log out and log back in to refresh your authentication token.");
            }
            
            String authToken = extractToken(httpRequest);
            TeamResponse team = teamManagementService.createTeam(request, userId, organizationId, role, authToken);
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
            
            if (role == null || role.trim().isEmpty()) {
                throw new RuntimeException("Access denied: User role is missing from token. Please log out and log back in.");
            }
            
            if (organizationId == null || organizationId == 0) {
                throw new RuntimeException("Access denied: Organization context is missing. Please log out and log back in to refresh your authentication token.");
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
    
    @GetMapping(value = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get all teams", description = "Retrieves all active teams in the organization. All roles can view.")
    public ResponseEntity<List<TeamResponse>> getAllTeams(HttpServletRequest httpRequest) {
        try {
            // Log authentication context for debugging
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                log.info("getAllTeams - Authentication: {}, Authorities: {}", auth.getName(), auth.getAuthorities());
            } else {
                log.warn("getAllTeams - No authentication found in SecurityContext");
            }
            
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            log.info("getAllTeams - UserId: {}, Role: {}, OrganizationId: {}", userId, role, organizationId);
            
            if (role == null || role.trim().isEmpty()) {
                log.error("Role is null or empty in getAllTeams. UserId: {}, OrganizationId: {}", userId, organizationId);
                throw new RuntimeException("Access denied: User role is missing from token. Please log out and log back in.");
            }
            
            if (organizationId == null || organizationId == 0) {
                log.error("Missing organizationId for userId: {}, role: {} in getAllTeams. Token may not have organizationId set.", userId, role);
                throw new RuntimeException("Access denied: Organization context is missing. Please ensure your account is associated with an organization. If you just registered, try logging out and logging back in.");
            }
            
            List<TeamResponse> teams = teamManagementService.getAllTeams(organizationId);
            log.info("Successfully retrieved {} teams for organizationId: {}", teams.size(), organizationId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            log.error("Error in getAllTeams: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : "Access denied: Unable to retrieve teams. Please check your authentication token.");
        }
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @Operation(summary = "Get my teams", description = "Retrieves teams where the logged-in user is a member. All roles can view.")
    public ResponseEntity<List<TeamResponse>> getMyTeams(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            String role = getRole(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            log.info("getMyTeams - UserId: {}, Role: {}, OrganizationId: {}", userId, role, organizationId);
            
            if (role == null || role.trim().isEmpty()) {
                log.error("Role is null or empty in getMyTeams. UserId: {}, OrganizationId: {}", userId, organizationId);
                throw new RuntimeException("Access denied: User role is missing from token. Please log out and log back in.");
            }
            
            if (organizationId == null || organizationId == 0) {
                log.error("Missing organizationId for userId: {}, role: {} in getMyTeams. Token may not have organizationId set.", userId, role);
                throw new RuntimeException("Access denied: Organization context is missing. Please ensure your account is associated with an organization. If you just registered, try logging out and logging back in.");
            }
            
            List<TeamResponse> teams = teamManagementService.getMyTeams(userId, organizationId);
            log.info("Successfully retrieved {} teams for userId: {}, organizationId: {}", teams.size(), userId, organizationId);
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            log.error("Error in getMyTeams: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : "Access denied: Unable to retrieve teams. Please check your authentication token.");
        }
    }
}

