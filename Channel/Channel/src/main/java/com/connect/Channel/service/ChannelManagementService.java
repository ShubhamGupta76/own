package com.connect.Channel.service;

import com.connect.Channel.dto.ChannelMemberResponse;
import com.connect.Channel.dto.ChannelResponse;
import com.connect.Channel.dto.CreateChannelRequest;
import com.connect.Channel.entity.Channel;
import com.connect.Channel.entity.ChannelMember;
import com.connect.Channel.repository.ChannelMemberRepository;
import com.connect.Channel.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChannelManagementService {
    
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WebClient webClient;
    
    @Value("${team.service.url:http://localhost:8103}")
    private String teamServiceUrl;
    
    
    @Transactional
    public ChannelResponse createChannel(Long teamId, CreateChannelRequest request, Long createdBy, Long organizationId, String role) {
        
        if (role.equals("EMPLOYEE")) {
            if (!isTeamMember(teamId, createdBy, organizationId)) {
                throw new RuntimeException("Access denied: You must be a team member to create channels");
            }
        } else if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Access denied: Only ADMIN, MANAGER, and EMPLOYEE (team members) can create channels");
        }
        
        
        if (channelRepository.findByNameAndTeamId(request.getName(), teamId).isPresent()) {
            throw new RuntimeException("Channel with name '" + request.getName() + "' already exists in this team");
        }
        
        
        Channel.ChannelType channelType;
        try {
            String typeValue = request.getType() != null ? request.getType() : "STANDARD";
            channelType = Channel.ChannelType.valueOf(typeValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid channel type: " + request.getType());
        }
        
        
        Channel channel = Channel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teamId(teamId)
                .organizationId(organizationId)
                .type(channelType)
                .createdBy(createdBy)
                .chatEnabled(request.getChatEnabled() != null ? request.getChatEnabled() : true)
                .fileEnabled(request.getFileEnabled() != null ? request.getFileEnabled() : true)
                .meetingEnabled(request.getMeetingEnabled() != null ? request.getMeetingEnabled() : true)
                .active(true)
                .build();
        
        channel = channelRepository.save(channel);
        
      
        ChannelMember creator = ChannelMember.builder()
                .channelId(channel.getId())
                .userId(createdBy)
                .organizationId(organizationId)
                .build();
        
        channelMemberRepository.save(creator);
        
        return mapToChannelResponse(channel);
    }
    
    
    @Transactional
    public ChannelMemberResponse addChannelMember(Long channelId, Long userId, Long currentUserId, Long organizationId, String role) {
        
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        
        if (!channel.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Channel does not belong to your organization");
        }
        
       
        if (role.equals("EMPLOYEE")) {
            
            if (!isTeamMember(channel.getTeamId(), currentUserId, organizationId)) {
                throw new RuntimeException("Access denied: You must be a team member to add channel members");
            }
        } else if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Access denied: Only ADMIN, MANAGER, and EMPLOYEE (team members) can add channel members");
        }
        
        
        if (channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            throw new RuntimeException("User is already a member of this channel");
        }
        
       
        ChannelMember member = ChannelMember.builder()
                .channelId(channelId)
                .userId(userId)
                .organizationId(organizationId)
                .build();
        
        member = channelMemberRepository.save(member);
        
        return mapToMemberResponse(member);
    }
    
    
    @Transactional
    public void removeChannelMember(Long channelId, Long userId, Long currentUserId, Long organizationId, String role) {
       
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        
        if (!channel.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Channel does not belong to your organization");
        }
        
       
        if (role.equals("EMPLOYEE")) {
           
            if (!isTeamMember(channel.getTeamId(), currentUserId, organizationId)) {
                throw new RuntimeException("Access denied: You must be a team member to remove channel members");
            }
        } else if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            throw new RuntimeException("Access denied: Only ADMIN, MANAGER, and EMPLOYEE (team members) can remove channel members");
        }
        
        
        ChannelMember member = channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this channel"));
        
        channelMemberRepository.delete(member);
    }
    
   
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByTeam(Long teamId, Long organizationId) {
        // Verify team belongs to organization (call Team service or check locally)
        List<Channel> channels = channelRepository.findByTeamIdAndActiveTrue(teamId);
        
        return channels.stream()
                .filter(channel -> channel.getOrganizationId().equals(organizationId))
                .map(this::mapToChannelResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get channel members
     */
    @Transactional(readOnly = true)
    public List<ChannelMemberResponse> getChannelMembers(Long channelId, Long organizationId) {
        // Verify channel belongs to organization
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        
        if (!channel.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Channel does not belong to your organization");
        }
        
        List<ChannelMember> members = channelMemberRepository.findByChannelId(channelId);
        
        return members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user is member of team
     * Calls Team Service to validate team membership
     */
    private boolean isTeamMember(Long teamId, Long userId, Long organizationId) {
        try {
            // Call Team Service to check membership
            java.util.Map response = webClient.get()
                    .uri(teamServiceUrl + "/api/teams/" + teamId + "/members/" + userId + "/check")
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();
            
            if (response != null && response.containsKey("isMember")) {
                return Boolean.TRUE.equals(response.get("isMember"));
            }
            return false;
        } catch (Exception e) {
            // If Team Service is not available, return false
            return false;
        }
    }
    
    /**
     * Map Channel entity to ChannelResponse DTO
     */
    private ChannelResponse mapToChannelResponse(Channel channel) {
        List<ChannelMember> members = channelMemberRepository.findByChannelId(channel.getId());
        List<ChannelMemberResponse> memberResponses = members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
        
        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .teamId(channel.getTeamId())
                .organizationId(channel.getOrganizationId())
                .type(channel.getType().name())
                .active(channel.getActive())
                .createdAt(channel.getCreatedAt())
                .members(memberResponses)
                .permissions(java.util.Collections.emptyList()) // Can be populated if needed
                .build();
    }
    
    /**
     * Map ChannelMember entity to ChannelMemberResponse DTO
     */
    private ChannelMemberResponse mapToMemberResponse(ChannelMember member) {
        return ChannelMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .channelId(member.getChannelId())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

