package com.connect.User.dto;

import com.connect.User.entity.User;
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
    private User.Role role;
    private Boolean isValid;
    private Boolean isFirstLogin;
    private String message;
}

