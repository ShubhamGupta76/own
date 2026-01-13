package com.connect.Team.service;

import com.connect.Team.dto.AddTeamMemberRequest;
import com.connect.Team.dto.CreateTeamRequest;
import com.connect.Team.dto.TeamMemberResponse;
import com.connect.Team.dto.TeamResponse;
import com.connect.Team.entity.Team;
import com.connect.Team.entity.TeamMember;
import com.connect.Team.repository.TeamMemberRepository;
import com.connect.Team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for team management operations
 * Handles team creation, member management, and auto-creates General channel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamManagementService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WebClient webClient;
    
    @Value("${channel.service.url:http://localhost:8084}")
    private String channelServiceUrl;
    
    /**
     * Create a new team
     * ADMIN and MANAGER can create teams
     * Auto-creates "General" channel
     */
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, Long createdBy, Long organizationId, String role) {
        // Validate role (only ADMIN and MANAGER can create teams)
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can create teams");
        }
        
        // Create team
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organizationId(organizationId)
                .createdBy(createdBy)
                .active(true)
                .build();
        
        team = teamRepository.save(team);
        
        // Add creator as team owner
        TeamMember owner = TeamMember.builder()
                .teamId(team.getId())
                .userId(createdBy)
                .organizationId(organizationId)
                .role(TeamMember.MemberRole.OWNER)
                .build();
        
        teamMemberRepository.save(owner);
        
        // Auto-create "General" channel
        try {
            createGeneralChannel(team.getId(), organizationId);
            log.info("General channel created for team: {}", team.getId());
        } catch (Exception e) {
            log.error("Failed to create General channel for team {}: {}", team.getId(), e.getMessage());
            // Don't fail team creation if channel creation fails
        }
        
        return mapToTeamResponse(team);
    }
    
    /**
     * Add a member to a team
     * ADMIN and MANAGER can add members
     */
    @Transactional
    public TeamMemberResponse addTeamMember(Long teamId, AddTeamMemberRequest request, Long organizationId, String role) {
        // Validate role (only ADMIN and MANAGER can add members)
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can add team members");
        }
        
        // Verify team exists and belongs to organization
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Team does not belong to your organization");
        }
        
        // Check if member already exists
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this team");
        }
        
        // Validate member role
        TeamMember.MemberRole memberRole;
        try {
            memberRole = TeamMember.MemberRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid member role: " + request.getRole());
        }
        
        // Create team member
        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(request.getUserId())
                .organizationId(organizationId)
                .role(memberRole)
                .build();
        
        member = teamMemberRepository.save(member);
        
        return mapToMemberResponse(member);
    }
    
    /**
     * Remove a member from a team
     * ADMIN and MANAGER can remove members
     */
    @Transactional
    public void removeTeamMember(Long teamId, Long userId, Long organizationId, String role) {
        // Validate role (only ADMIN and MANAGER can remove members)
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can remove team members");
        }
        
        // Verify team exists and belongs to organization
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Team does not belong to your organization");
        }
        
        // Find and remove member
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this team"));
        
        // Prevent removing the team owner
        if (member.getRole() == TeamMember.MemberRole.OWNER) {
            throw new RuntimeException("Cannot remove team owner");
        }
        
        teamMemberRepository.delete(member);
    }
    
    /**
     * Get all teams in organization
     * All roles can view teams
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams(Long organizationId) {
        List<Team> teams = teamRepository.findByOrganizationIdAndActiveTrue(organizationId);
        
        return teams.stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get teams for logged-in user
     * Returns teams where user is a member
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(Long userId, Long organizationId) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        
        // Filter by organization and get teams
        return memberships.stream()
                .filter(m -> m.getOrganizationId().equals(organizationId))
                .map(m -> teamRepository.findById(m.getTeamId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(Team::getActive)
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user is member of team
     */
    @Transactional(readOnly = true)
    public boolean isTeamMember(Long teamId, Long userId) {
        return teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
    }
    
    /**
     * Get team by ID (for validation)
     */
    @Transactional(readOnly = true)
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }
    
    /**
     * Create General channel for a team
     * Calls Channel Service to create the channel
     */
    private void createGeneralChannel(Long teamId, Long organizationId) {
        try {
            java.util.Map<String, Object> channelRequest = new java.util.HashMap<>();
            channelRequest.put("name", "General");
            channelRequest.put("description", "General discussion channel");
            channelRequest.put("teamId", teamId);
            channelRequest.put("type", "STANDARD");
            channelRequest.put("chatEnabled", true);
            channelRequest.put("fileEnabled", true);
            channelRequest.put("meetingEnabled", true);
            
            webClient.post()
                    .uri(channelServiceUrl + "/api/channels/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(channelRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating General channel: {}", e.getMessage());
            throw new RuntimeException("Failed to create General channel: " + e.getMessage());
        }
    }
    
    /**
     * Map Team entity to TeamResponse DTO
     */
    private TeamResponse mapToTeamResponse(Team team) {
        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
        List<TeamMemberResponse> memberResponses = members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
        
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .organizationId(team.getOrganizationId())
                .active(team.getActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .members(memberResponses)
                .build();
    }
    
    /**
     * Map TeamMember entity to TeamMemberResponse DTO
     */
    private TeamMemberResponse mapToMemberResponse(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .teamId(member.getTeamId())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

