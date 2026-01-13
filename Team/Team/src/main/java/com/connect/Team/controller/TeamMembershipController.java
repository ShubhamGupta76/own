package com.connect.Team.controller;

import com.connect.Team.service.TeamManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal controller for team membership checks
 * Called by Channel Service to validate team membership
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamMembershipController {
    
    private final TeamManagementService teamManagementService;
    
    /**
     * Check if user is member of team
     * GET /api/teams/{teamId}/members/{userId}/check
     * Internal endpoint for Channel Service
     */
    @GetMapping("/{teamId}/members/{userId}/check")
    public ResponseEntity<Map<String, Boolean>> checkTeamMembership(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        try {
            boolean isMember = teamManagementService.isTeamMember(teamId, userId);
            return ResponseEntity.ok(Map.of("isMember", isMember));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isMember", false));
        }
    }
}

