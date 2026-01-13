package com.connect.User.repository;

import com.connect.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email and organization ID
     */
    Optional<User> findByEmailAndOrganizationId(String email, Long organizationId);
    
    /**
     * Find all users in an organization
     */
    List<User> findByOrganizationId(Long organizationId);
    
    /**
     * Find active users in an organization
     */
    List<User> findByOrganizationIdAndActiveTrue(Long organizationId);
    
    /**
     * Find users by role in an organization
     */
    List<User> findByOrganizationIdAndRole(Long organizationId, User.Role role);
    
    /**
     * Check if user exists by email and organization ID
     */
    boolean existsByEmailAndOrganizationId(String email, Long organizationId);
    
    /**
     * Find employee by email and role (searches all organizations)
     * Used for employee login validation
     */
    Optional<User> findByEmailAndRole(String email, User.Role role);
    
    /**
     * Find user by ID and organization ID
     */
    Optional<User> findByIdAndOrganizationId(Long id, Long organizationId);
}

