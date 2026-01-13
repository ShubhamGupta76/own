package com.connect.Auth.service;

import com.connect.Auth.dto.AuthResponse;
import com.connect.Auth.dto.LoginRequest;
import com.connect.Auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for employee authentication
 * Calls User Service to validate employee credentials
 */
@Service
@RequiredArgsConstructor
public class EmployeeAuthService {
    
    private final JwtUtil jwtUtil;
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    private final WebClient webClient;
    
    /**
     * Login employee and generate JWT token
     */
    @Transactional(readOnly = true)
    public AuthResponse loginEmployee(LoginRequest request) {
        // Call User Service to validate employee credentials
        Map<String, Object> validationRequest = new HashMap<>();
        validationRequest.put("email", request.getEmail());
        validationRequest.put("password", request.getPassword());
        
        EmployeeValidationResponse validationResponse = webClient.post()
                .uri(userServiceUrl + "/api/employees/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validationRequest)
                .retrieve()
                .bodyToMono(EmployeeValidationResponse.class)
                .block();
        
        if (validationResponse == null || !validationResponse.getIsValid()) {
            throw new RuntimeException(validationResponse != null ? 
                    validationResponse.getMessage() : "Invalid email or password");
        }
        
        // Generate JWT token with employee details
        // Role comes as string from JSON deserialization
        String roleString = validationResponse.getRole(); // "EMPLOYEE" as string
        String token = jwtUtil.generateToken(
                validationResponse.getUserId(),
                validationResponse.getEmail(),
                roleString,
                validationResponse.getOrganizationId()
        );
        
        return AuthResponse.builder()
                .token(token)
                .userId(validationResponse.getUserId())
                .email(validationResponse.getEmail())
                .role(roleString) // "EMPLOYEE" string
                .organizationId(validationResponse.getOrganizationId())
                .message("Login successful")
                .build();
    }
    
    /**
     * EmployeeValidationResponse DTO for deserialization
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmployeeValidationResponse {
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private Long organizationId;
        private String role; // "EMPLOYEE" as string
        private Boolean isValid;
        private Boolean isFirstLogin;
        private String message;
    }
}

