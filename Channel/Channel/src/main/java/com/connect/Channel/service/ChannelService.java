package com.connect.Channel.service;

import com.connect.Channel.dto.ChannelMemberResponse;
import com.connect.Channel.dto.ChannelPermissionResponse;
import com.connect.Channel.dto.ChannelResponse;
import com.connect.Channel.entity.Channel;
import com.connect.Channel.entity.ChannelMember;
import com.connect.Channel.entity.ChannelPermission;
import com.connect.Channel.repository.ChannelMemberRepository;
import com.connect.Channel.repository.ChannelPermissionRepository;
import com.connect.Channel.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {
    
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final ChannelPermissionRepository channelPermissionRepository;
    
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByOrganization(Long organizationId) {
        List<Channel> channels = channelRepository.findByOrganizationId(organizationId);
        
        return channels.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByTeam(Long teamId, Long organizationId) {
        List<Channel> channels = channelRepository.findByTeamId(teamId);
        
        return channels.stream()
                .filter(channel -> channel.getOrganizationId().equals(organizationId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ChannelResponse getChannelById(Long channelId, Long organizationId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        
        if (!channel.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Channel does not belong to your organization");
        }
        
        return mapToResponse(channel);
    }
    
    private ChannelResponse mapToResponse(Channel channel) {
        List<ChannelMember> members = channelMemberRepository.findByChannelId(channel.getId());
        List<ChannelPermission> permissions = channelPermissionRepository.findByChannelId(channel.getId());
        
        return ChannelResponse.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .teamId(channel.getTeamId())
                .organizationId(channel.getOrganizationId())
                .type(channel.getType().name())
                .active(channel.getActive())
                .chatEnabled(channel.getChatEnabled())
                .fileEnabled(channel.getFileEnabled())
                .meetingEnabled(channel.getMeetingEnabled())
                .createdAt(channel.getCreatedAt())
                .members(members.stream()
                        .map(m -> ChannelMemberResponse.builder()
                                .id(m.getId())
                                .userId(m.getUserId())
                                .channelId(m.getChannelId())
                                .joinedAt(m.getJoinedAt())
                                .build())
                        .collect(Collectors.toList()))
                .permissions(permissions.stream()
                        .map(p -> ChannelPermissionResponse.builder()
                                .id(p.getId())
                                .channelId(p.getChannelId())
                                .userId(p.getUserId())
                                .teamId(p.getTeamId())
                                .permissionType(p.getPermissionType().name())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

