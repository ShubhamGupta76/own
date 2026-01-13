package com.connect.Meeting.controller;

import com.connect.Meeting.entity.MeetingPolicy;
import com.connect.Meeting.service.MeetingPolicyService;
import com.connect.Meeting.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/meetings/policies")
@RequiredArgsConstructor
@Tag(name = "Meeting Policy Management", description = "Admin APIs for controlling meeting feature")
@SecurityRequirement(name = "bearerAuth")
public class MeetingPolicyController {
    
    private final MeetingPolicyService meetingPolicyService;
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
    @Operation(summary = "Get meeting policy", description = "Retrieves meeting policy configuration for the organization.")
    public ResponseEntity<MeetingPolicy> getMeetingPolicy(HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        return ResponseEntity.ok(meetingPolicyService.getMeetingPolicy(organizationId));
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update meeting policy", description = "Updates meeting policy (enable/disable, max duration, max participants).")
    public ResponseEntity<MeetingPolicy> updateMeetingPolicy(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        Long organizationId = getOrganizationId(httpRequest);
        if (organizationId == null) {
            throw new RuntimeException("Organization not found");
        }
        
        Boolean enabled = request.get("enabled") != null ? (Boolean) request.get("enabled") : null;
        Integer maxDurationMinutes = request.get("maxDurationMinutes") != null ? 
                ((Number) request.get("maxDurationMinutes")).intValue() : null;
        Integer maxParticipants = request.get("maxParticipants") != null ? 
                ((Number) request.get("maxParticipants")).intValue() : null;
        
        return ResponseEntity.ok(meetingPolicyService.updateMeetingPolicy(organizationId, enabled, maxDurationMinutes, maxParticipants));
    }
}

