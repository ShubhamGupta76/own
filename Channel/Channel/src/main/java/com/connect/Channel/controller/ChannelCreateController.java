package com.connect.Channel.controller;

import com.connect.Channel.dto.ChannelResponse;
import com.connect.Channel.entity.Channel;
import com.connect.Channel.repository.ChannelRepository;
import com.connect.Channel.service.ChannelManagementService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal controller for creating channels (called by Team Service)
 * This endpoint is used when Team Service auto-creates "General" channel
 */
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelCreateController {
    
    private final ChannelRepository channelRepository;
    
    /**
     * Internal endpoint for creating channel
     * POST /api/channels/create
     * Called by Team Service to create General channel
     */
    @PostMapping("/create")
    @Operation(summary = "Create channel (internal)", description = "Internal endpoint for creating channels. Called by Team Service.")
    public ResponseEntity<Channel> createChannel(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Long teamId = ((Number) request.get("teamId")).longValue();
            String type = (String) request.getOrDefault("type", "STANDARD");
            Boolean chatEnabled = request.get("chatEnabled") != null ? (Boolean) request.get("chatEnabled") : true;
            Boolean fileEnabled = request.get("fileEnabled") != null ? (Boolean) request.get("fileEnabled") : true;
            Boolean meetingEnabled = request.get("meetingEnabled") != null ? (Boolean) request.get("meetingEnabled") : true;
            
            // Get organizationId from request or set default
            Long organizationId = request.get("organizationId") != null ? 
                    ((Number) request.get("organizationId")).longValue() : null;
            
            if (organizationId == null) {
                // Try to get from existing channel in team
                var existingChannel = channelRepository.findByTeamId(teamId).stream().findFirst();
                if (existingChannel.isPresent()) {
                    organizationId = existingChannel.get().getOrganizationId();
                } else {
                    throw new RuntimeException("Organization ID is required");
                }
            }
            
            Channel.ChannelType channelType;
            try {
                channelType = Channel.ChannelType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                channelType = Channel.ChannelType.STANDARD;
            }
            
            Channel channel = Channel.builder()
                    .name(name)
                    .description(description)
                    .teamId(teamId)
                    .organizationId(organizationId)
                    .type(channelType)
                    .chatEnabled(chatEnabled)
                    .fileEnabled(fileEnabled)
                    .meetingEnabled(meetingEnabled)
                    .active(true)
                    .build();
            
            channel = channelRepository.save(channel);
            return ResponseEntity.status(201).body(channel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create channel: " + e.getMessage());
        }
    }
}

