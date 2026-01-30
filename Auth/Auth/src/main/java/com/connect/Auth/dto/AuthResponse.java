package com.connect.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String organizationId;
    private String message;
}

