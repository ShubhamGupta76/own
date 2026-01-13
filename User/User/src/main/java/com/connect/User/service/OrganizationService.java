package com.connect.User.service;

import com.connect.User.dto.OrganizationRequest;
import com.connect.User.entity.Organization;
import com.connect.User.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for organization management
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    
    /**
     * Create a new organization
     */
    @Transactional
    public Organization createOrganization(OrganizationRequest request, Long adminId) {
        // Check if organization name already exists
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Organization with name " + request.getName() + " already exists");
        }
        
        // Check if domain already exists (if provided)
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
        
        return organizationRepository.save(organization);
    }
    
    /**
     * Get organization by ID (admin can only access their own organization)
     */
    @Transactional(readOnly = true)
    public Organization getOrganization(Long organizationId, Long adminId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        // Verify admin owns this organization
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access your own organization");
        }
        
        return organization;
    }
    
    /**
     * Get organization by admin ID
     */
    @Transactional(readOnly = true)
    public Organization getOrganizationByAdminId(Long adminId) {
        return organizationRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Organization not found for admin"));
    }
}

