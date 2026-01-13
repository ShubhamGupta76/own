package com.connect.Team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for team member response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    
    private Long id;
    private Long userId;
    private Long teamId;
    private String role;
    private LocalDateTime joinedAt;
}

