package com.connect.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for employee profile response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileResponse {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private Long organizationId;
    private String role;
    private Boolean active;
    private Boolean isFirstLogin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

