package com.connect.User.service;

import com.connect.User.dto.PolicyRequest;
import com.connect.User.entity.Organization;
import com.connect.User.entity.Policy;
import com.connect.User.repository.OrganizationRepository;
import com.connect.User.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for policy management
 */
@Service
@RequiredArgsConstructor
public class PolicyService {
    
    private final PolicyRepository policyRepository;
    private final OrganizationRepository organizationRepository;
    
    /**
     * Get or create policy for an organization
     */
    public Policy getOrCreatePolicy(String organizationId, Policy.PolicyType policyType, String adminId) {
        // Verify organization exists and admin owns it
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only manage policies for your own organization");
        }
        
        return policyRepository.findByOrganizationIdAndPolicyType(organizationId, policyType)
                .orElseGet(() -> {
                    Policy policy = Policy.builder()
                            .organizationId(organizationId)
                            .policyType(policyType)
                            .enabled(true) // Default enabled
                            .build();
                    return policyRepository.save(policy);
                });
    }
    
    /**
     * Update policy (enable/disable)
     */
    public Policy updatePolicy(String organizationId, PolicyRequest request, String adminId) {
        // Verify organization exists and admin owns it
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only manage policies for your own organization");
        }
        
        // Validate policy type
        Policy.PolicyType policyType;
        try {
            policyType = Policy.PolicyType.valueOf(request.getPolicyType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid policy type: " + request.getPolicyType());
        }
        
        Policy policy = getOrCreatePolicy(organizationId, policyType, adminId);
        policy.setEnabled(request.getEnabled());
        return policyRepository.save(policy);
    }
    
    /**
     * Get all policies for an organization
     */
    public List<Policy> getPoliciesByOrganization(String organizationId, String adminId) {
        // Verify organization exists and admin owns it
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!organization.getAdminId().equals(adminId)) {
            throw new RuntimeException("Access denied: You can only access policies for your own organization");
        }
        
        return policyRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Get specific policy
     */
    public Policy getPolicy(String organizationId, Policy.PolicyType policyType, String adminId) {
        return getOrCreatePolicy(organizationId, policyType, adminId);
    }
}

