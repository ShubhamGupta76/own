package com.connect.User.repository;

import com.connect.User.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Policy entity
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    /**
     * Find policy by organization ID and policy type
     */
    Optional<Policy> findByOrganizationIdAndPolicyType(Long organizationId, Policy.PolicyType policyType);
    
    /**
     * Find all policies for an organization
     */
    List<Policy> findByOrganizationId(Long organizationId);
    
    /**
     * Check if policy exists for organization
     */
    boolean existsByOrganizationIdAndPolicyType(Long organizationId, Policy.PolicyType policyType);
}

