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


@Service
@RequiredArgsConstructor
public class EmployeeAuthService {
    
    private final JwtUtil jwtUtil;
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    private final WebClient webClient;
    
    
    @Transactional(readOnly = true)
    public AuthResponse loginEmployee(LoginRequest request) {
        
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
        
       
        String roleString = validationResponse.getRole(); 
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
    
   
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmployeeValidationResponse {
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private Long organizationId;
        private String role; 
        private Boolean isValid;
        private Boolean isFirstLogin;
        private String message;
    }
}

