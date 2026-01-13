package com.connect.User.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating policy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {
    
    @NotBlank(message = "Policy type is required")
    private String policyType; // CHAT, MEETING, FILE_SHARING
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}

