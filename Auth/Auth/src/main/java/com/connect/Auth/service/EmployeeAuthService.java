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
    
    @Value("${user.service.url:http://localhost:8102}")
    private String userServiceUrl;
    
    private final WebClient webClient;
    
    
    @Transactional(readOnly = true)
    public AuthResponse loginEmployee(LoginRequest request) {
        try {
            Map<String, Object> validationRequest = new HashMap<>();
            validationRequest.put("email", request.getEmail());
            validationRequest.put("password", request.getPassword());
            
            EmployeeValidationResponse validationResponse = webClient.post()
                    .uri(userServiceUrl + "/api/employees/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(validationRequest)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                            response -> {
                                // Try to extract error message from response body
                                return response.bodyToMono(String.class)
                                        .defaultIfEmpty("Unable to validate employee credentials")
                                        .flatMap(errorBody -> {
                                            String errorMsg = "Unable to validate employee credentials. ";
                                            if (errorBody != null && !errorBody.isEmpty()) {
                                                errorMsg += errorBody;
                                            } else {
                                                errorMsg += "Please ensure the User service is running and the employee account exists.";
                                            }
                                            return Mono.error(new RuntimeException(errorMsg));
                                        });
                            })
                    .bodyToMono(EmployeeValidationResponse.class)
                    .block();
            
            if (validationResponse == null) {
                throw new RuntimeException("Invalid email or password. Unable to validate credentials. Please contact your administrator if you believe this is an error.");
            }
            
            if (!validationResponse.getIsValid()) {
                String errorMessage = validationResponse.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Invalid email or password. Please check your credentials. If you just registered, make sure your administrator has set your password.";
                }
                throw new RuntimeException(errorMessage);
            }
            
            // CRITICAL: EMPLOYEE users MUST have organizationId assigned
            if ("EMPLOYEE".equalsIgnoreCase(validationResponse.getRole()) && 
                (validationResponse.getOrganizationId() == null || validationResponse.getOrganizationId() == 0)) {
                throw new RuntimeException("Employee account is not assigned to an organization. Please contact your administrator.");
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
                    .firstName(validationResponse.getFirstName())
                    .lastName(validationResponse.getLastName())
                    .role(roleString) 
                    .organizationId(validationResponse.getOrganizationId())
                    .message("Login successful")
                    .build();
        } catch (org.springframework.web.reactive.function.client.WebClientException e) {
            throw new RuntimeException("Cannot connect to User service. Please ensure the User service is running at " + userServiceUrl + ". Error: " + e.getMessage());
        } catch (RuntimeException e) {
            // Re-throw RuntimeExceptions as-is (they already have proper messages)
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to login employee: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
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

