package com.connect.Team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    
    @NotBlank(message = "Team name is required")
    private String name;
    
    private String description;
}

