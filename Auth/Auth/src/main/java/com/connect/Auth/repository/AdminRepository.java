package com.connect.Auth.repository;

import com.connect.Auth.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Admin entity
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * Find admin by email
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * Check if admin exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find admin by email and active status
     */
    Optional<Admin> findByEmailAndActiveTrue(String email);
}

