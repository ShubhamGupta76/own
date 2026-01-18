package com.connect.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for employee validation response (used by Auth Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeValidationResponse {
    
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Long organizationId;
    private String role; // Changed from User.Role enum to String for API compatibility
    private Boolean isValid;
    private Boolean isFirstLogin;
    private String message;
}

