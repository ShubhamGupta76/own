package com.connect.User.dto;

import com.connect.User.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for organization creation response
 * Includes organization details and new JWT token with organizationId
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    
    private Organization organization;
    private String token;
    private Long userId;
    private String email;
    private String role;
    private Long organizationId;
    private String message;
}

