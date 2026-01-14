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

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamManagementService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WebClient webClient;
    
    @Value("${channel.service.url:http://localhost:8084}")
    private String channelServiceUrl;
    
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, Long createdBy, Long organizationId, String role) {
        if (role == null || role.trim().isEmpty()) {
            log.error("Role is null or empty when creating team. UserId: {}, OrganizationId: {}", createdBy, organizationId);
            throw new RuntimeException("User role is missing");
        }
        
        String normalizedRole = role.trim().toUpperCase();
        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("MANAGER")) {
            log.warn("User with role '{}' attempted to create team. UserId: {}, OrganizationId: {}", role, createdBy, organizationId);
            throw new RuntimeException("Only ADMIN and MANAGER roles can create teams");
        }
        
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organizationId(organizationId)
                .createdBy(createdBy)
                .active(true)
                .build();
        
        team = teamRepository.save(team);
        
        TeamMember owner = TeamMember.builder()
                .teamId(team.getId())
                .userId(createdBy)
                .organizationId(organizationId)
                .role(TeamMember.MemberRole.OWNER)
                .build();
        
        teamMemberRepository.save(owner);
        
        try {
            createGeneralChannel(team.getId(), organizationId);
            log.info("General channel created for team: {}", team.getId());
        } catch (Exception e) {
            log.error("Failed to create General channel for team {}: {}", team.getId(), e.getMessage());
        }
        
        return mapToTeamResponse(team);
    }
    
    @Transactional
    public TeamMemberResponse addTeamMember(Long teamId, AddTeamMemberRequest request, Long organizationId, String role) {
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can add team members");
        }
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Team does not belong to your organization");
        }
        
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new RuntimeException("User is already a member of this team");
        }
        
        TeamMember.MemberRole memberRole;
        try {
            memberRole = TeamMember.MemberRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid member role: " + request.getRole());
        }
        
        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(request.getUserId())
                .organizationId(organizationId)
                .role(memberRole)
                .build();
        
        member = teamMemberRepository.save(member);
        
        return mapToMemberResponse(member);
    }
    
    @Transactional
    public void removeTeamMember(Long teamId, Long userId, Long organizationId, String role) {
        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Only ADMIN and MANAGER can remove team members");
        }
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Team does not belong to your organization");
        }
        
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this team"));
        
        if (member.getRole() == TeamMember.MemberRole.OWNER) {
            throw new RuntimeException("Cannot remove team owner");
        }
        
        teamMemberRepository.delete(member);
    }
    
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams(Long organizationId) {
        List<Team> teams = teamRepository.findByOrganizationIdAndActiveTrue(organizationId);
        
        return teams.stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams(Long userId, Long organizationId) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        
        return memberships.stream()
                .filter(m -> m.getOrganizationId().equals(organizationId))
                .map(m -> teamRepository.findById(m.getTeamId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .filter(Team::getActive)
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isTeamMember(Long teamId, Long userId) {
        return teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
    }
    
    @Transactional(readOnly = true)
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }
    
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

