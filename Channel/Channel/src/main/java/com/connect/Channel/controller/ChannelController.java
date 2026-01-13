package com.connect.Channel.controller;

import com.connect.Channel.dto.ChannelResponse;
import com.connect.Channel.service.ChannelService;
import com.connect.Channel.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "Channel Management", description = "Admin read-only APIs for viewing channels and permissions")
@SecurityRequirement(name = "bearerAuth")
public class ChannelController {
    
    private final ChannelService channelService;
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
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all channels", description = "Retrieves all channels in the admin's organization.")
    public ResponseEntity<List<ChannelResponse>> getChannels(HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(channelService.getChannelsByOrganization(organizationId));
    }
    
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get channels by team", description = "Retrieves all channels under a specific team.")
    public ResponseEntity<List<ChannelResponse>> getChannelsByTeam(
            @PathVariable Long teamId,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(channelService.getChannelsByTeam(teamId, organizationId));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get channel", description = "Retrieves channel details with members and permissions.")
    public ResponseEntity<ChannelResponse> getChannel(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(channelService.getChannelById(id, organizationId));
    }
}

