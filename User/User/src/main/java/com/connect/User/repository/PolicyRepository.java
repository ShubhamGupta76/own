package com.connect.User.repository;

import com.connect.User.entity.Policy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Policy entity
 */
@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {
    
   
    Optional<Policy> findByOrganizationIdAndPolicyType(String organizationId, Policy.PolicyType policyType);
    
    /**
     * Find all policies for an organization
     */
    List<Policy> findByOrganizationId(String organizationId);
    
    /**
     * Check if policy exists for organization
     */
    boolean existsByOrganizationIdAndPolicyType(String organizationId, Policy.PolicyType policyType);
}

