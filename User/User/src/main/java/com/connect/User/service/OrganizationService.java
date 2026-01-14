package com.connect.User.service;

import com.connect.User.dto.OrganizationRequest;
import com.connect.User.entity.Organization;
import com.connect.User.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
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
    
    private void syncOrganizationIdToAuthService(Long adminId, Long organizationId) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("adminId", adminId);
            requestBody.put("organizationId", organizationId);
            
            webClient.post()
                    .uri(authServiceUrl + "/api/auth/admin/organization-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error syncing organizationId to Auth service: {}", e.getMessage());
            throw new RuntimeException("Failed to sync organizationId to Auth service: " + e.getMessage());
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
                .orElseThrow(() -> new RuntimeException("Organization not found for admin"));
    }
}

