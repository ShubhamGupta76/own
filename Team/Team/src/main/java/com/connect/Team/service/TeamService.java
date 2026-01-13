package com.connect.Team.service;

import com.connect.Team.dto.TeamMemberResponse;
import com.connect.Team.dto.TeamResponse;
import com.connect.Team.entity.Team;
import com.connect.Team.entity.TeamMember;
import com.connect.Team.repository.TeamMemberRepository;
import com.connect.Team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for team management (read-only for admin)
 */
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * Get all teams in an organization (admin read-only)
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByOrganization(Long organizationId) {
        List<Team> teams = teamRepository.findByOrganizationId(organizationId);
        
        return teams.stream()
                .map(team -> {
                    List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
                    List<TeamMemberResponse> memberResponses = members.stream()
                            .map(member -> TeamMemberResponse.builder()
                                    .id(member.getId())
                                    .userId(member.getUserId())
                                    .teamId(member.getTeamId())
                                    .role(member.getRole().name())
                                    .joinedAt(member.getJoinedAt())
                                    .build())
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
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get team by ID with members (admin read-only)
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long teamId, Long organizationId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        // Verify team belongs to organization
        if (!team.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Team does not belong to your organization");
        }
        
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        List<TeamMemberResponse> memberResponses = members.stream()
                .map(member -> TeamMemberResponse.builder()
                        .id(member.getId())
                        .userId(member.getUserId())
                        .teamId(member.getTeamId())
                        .role(member.getRole().name())
                        .joinedAt(member.getJoinedAt())
                        .build())
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
}

