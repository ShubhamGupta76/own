package com.connect.User.repository;

import com.connect.User.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find user by email and organization ID
     */
    Optional<User> findByEmailAndOrganizationId(String email, String organizationId);
    
    /**
     * Find all users in an organization
     */
    List<User> findByOrganizationId(String organizationId);
    
    /**
     * Find active users in an organization
     */
    List<User> findByOrganizationIdAndActiveTrue(String organizationId);
    
    /**
     * Find users by role in an organization
     */
    List<User> findByOrganizationIdAndRole(String organizationId, User.Role role);
    
    /**
     * Check if user exists by email and organization ID
     */
    boolean existsByEmailAndOrganizationId(String email, String organizationId);
    
    /**
     * Find employee by email and role (searches all organizations)
     * Used for employee login validation
     */
    Optional<User> findByEmailAndRole(String email, User.Role role);
    
    /**
     * Find user by ID and organization ID
     */
    Optional<User> findByIdAndOrganizationId(String id, String organizationId);
}

