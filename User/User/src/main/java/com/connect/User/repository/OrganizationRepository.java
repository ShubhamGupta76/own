package com.connect.User.repository;

import com.connect.User.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Organization entity
 */
@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {
    
    /**
     * Find organization by name
     */
    Optional<Organization> findByName(String name);
    
    /**
     * Find organization by domain
     */
    Optional<Organization> findByDomain(String domain);
    
    /**
     * Find all active organizations
     */
    List<Organization> findByActiveTrue();
    
    /**
     * Find organization by admin ID
     */
    Optional<Organization> findByAdminId(String adminId);
}

