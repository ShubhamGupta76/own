package com.connect.User.service;

import com.connect.User.dto.AuthResponse;
import com.connect.User.dto.OrganizationRequest;
import com.connect.User.dto.OrganizationResponse;
import com.connect.User.entity.Organization;
import com.connect.User.entity.User;
import com.connect.User.repository.OrganizationRepository;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    
    @Value("${auth.service.url:http://localhost:8101}")
    private String authServiceUrl;
    
    @Transactional
    public Organization createOrganization(OrganizationRequest request, Long adminId) {
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Organization with name " + request.getName() + " already exists");
        }
        
        if (request.getDomain() != null && !request.getDomain().isEmpty()) {
            if (organizationRepository.findByDomain(request.getDomain()).isPresent()) {
                throw new RuntimeException("Organization with domain " + request.getDomain() + " already exists");
            }
        }
        
        Organization organization = Organization.builder()
                .name(request.getName())
                .domain(request.getDomain())
                .adminId(adminId)
                .active(true)
                .build();
        
        organization = organizationRepository.save(organization);
        
        try {
            syncOrganizationIdToAuthService(adminId, organization.getId());
            log.info("Successfully synced organizationId {} to Auth service for admin {}", organization.getId(), adminId);
        } catch (Exception e) {
            log.error("Failed to sync organizationId to Auth service for admin {}: {}", adminId, e.getMessage());
        }
        
        return organization;
    }

    @Transactional
    public OrganizationResponse createOrganizationWithToken(OrganizationRequest request, Long adminId) {
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Organization with name " + request.getName() + " already exists");
        }
        
        if (request.getDomain() != null && !request.getDomain().isEmpty()) {
            if (organizationRepository.findByDomain(request.getDomain()).isPresent()) {
                throw new RuntimeException("Organization with domain " + request.getDomain() + " already exists");
            }
        }
        
        Organization organization = Organization.builder()
                .name(request.getName())
                .domain(request.getDomain())
                .adminId(adminId)
                .active(true)
                .build();
        
        organization = organizationRepository.save(organization);
        
        try {
            AuthResponse authResponse = syncOrganizationIdToAuthService(adminId, organization.getId());
            log.info("Successfully synced organizationId {} to Auth service for admin {}", organization.getId(), adminId);
            
            // Create User record for the admin in User service
            createAdminUserRecord(authResponse, organization.getId(), adminId);
            
            return OrganizationResponse.builder()
                    .organization(organization)
                    .token(authResponse.getToken())
                    .userId(authResponse.getUserId())
                    .email(authResponse.getEmail())
                    .role(authResponse.getRole())
                    .organizationId(authResponse.getOrganizationId())
                    .message(authResponse.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to sync organizationId to Auth service for admin {}: {}", adminId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync organizationId to Auth service: " + e.getMessage());
        }
    }
    
    private AuthResponse syncOrganizationIdToAuthService(Long adminId, Long organizationId) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("adminId", adminId);
            requestBody.put("organizationId", organizationId);
            
            log.info("Calling Auth service to update organizationId: {} for admin: {} at URL: {}", 
                    organizationId, adminId, authServiceUrl + "/api/v1/auth/admin/organization-id");
            
            AuthResponse response = webClient.post()
                    .uri(authServiceUrl + "/api/v1/auth/admin/organization-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        clientResponse -> {
                            log.error("Auth service returned error status: {} for admin: {}", 
                                    clientResponse.statusCode(), adminId);
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        String errorMsg = "Auth service error: " + clientResponse.statusCode() + " - " + body;
                                        log.error("Auth service error details: {}", errorMsg);
                                        return Mono.error(new RuntimeException(errorMsg));
                                    });
                        })
                    .bodyToMono(AuthResponse.class)
                    .block();
            
            if (response == null) {
                log.error("Auth service returned null response for admin: {}", adminId);
                throw new RuntimeException("Auth service returned null response. Please check that Auth service is running.");
            }
            
            if (response.getToken() == null || response.getToken().isEmpty()) {
                log.error("Auth service returned response without token for admin: {}", adminId);
                throw new RuntimeException("Auth service did not return a valid token. Please check Auth service logs.");
            }
            
            log.info("Auth service response received successfully: token present = true, userId = {}, organizationId = {}", 
                    response.getUserId(), response.getOrganizationId());
            return response;
        } catch (org.springframework.web.reactive.function.client.WebClientException e) {
            log.error("WebClient error syncing organizationId to Auth service for admin {}: {}", adminId, e.getMessage(), e);
            String errorMsg = "Cannot connect to Auth service. Please ensure Auth service is running at " + authServiceUrl;
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                errorMsg = "Auth service is not running or not accessible at " + authServiceUrl + ". Please start the Auth service.";
            } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                errorMsg = "Auth service request timed out. Please check network connectivity and Auth service status.";
            }
            throw new RuntimeException(errorMsg);
        } catch (RuntimeException e) {
            // Re-throw RuntimeExceptions as-is (they already have proper messages)
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error syncing organizationId to Auth service for admin {}: {}", adminId, e.getMessage(), e);
            throw new RuntimeException("Failed to sync organizationId to Auth service: " + 
                    (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
    
    @Transactional(readOnly = true)
    public Organization getOrganization(Long organizationId, Long adminId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access your own organization");
        }
        
        return organization;
    }
    
    @Transactional(readOnly = true)
    public Organization getOrganizationByAdminId(Long adminId) {
        return organizationRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Organization not found. Please create an organization first."));
    }
    
    /**
     * Create User record for admin in User service
     * This ensures the admin has a User record that can be accessed by other services
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createAdminUserRecord(AuthResponse authResponse, Long organizationId, Long adminId) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmailAndOrganizationId(authResponse.getEmail(), organizationId)) {
                log.info("User record already exists for admin {} in organization {}", adminId, organizationId);
                return;
            }
            
            // Validate required fields
            if (authResponse.getEmail() == null || authResponse.getEmail().isEmpty()) {
                log.error("Cannot create User record: email is missing in AuthResponse for admin {}", adminId);
                return;
            }
            
            // Create User record with admin details
            User adminUser = User.builder()
                    .email(authResponse.getEmail())
                    .firstName(authResponse.getFirstName() != null && !authResponse.getFirstName().isEmpty() 
                            ? authResponse.getFirstName() : "Admin")
                    .lastName(authResponse.getLastName() != null && !authResponse.getLastName().isEmpty() 
                            ? authResponse.getLastName() : "User")
                    .organizationId(organizationId)
                    .role(User.Role.ADMIN)
                    .active(true)
                    .isFirstLogin(false) // Admin has already logged in during registration
                    .build();
            
            adminUser = userRepository.save(adminUser);
            log.info("Successfully created User record for admin {} with User ID {} in organization {}", 
                    adminId, adminUser.getId(), organizationId);
        } catch (Exception e) {
            log.error("Failed to create User record for admin {} in organization {}: {}", 
                    adminId, organizationId, e.getMessage(), e);
            // Don't throw exception - organization creation should still succeed
            // The user can be created later if needed
        }
    }
}

