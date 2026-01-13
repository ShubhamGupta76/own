package com.connect.Team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a member to a team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTeamMemberRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String role = "MEMBER"; // OWNER, ADMIN, or MEMBER (default)
}

