package com.connect.User.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    
    @NotBlank(message = "Organization name is required")
    private String name;
    
    private String domain; // Optional: company domain for email validation
}

