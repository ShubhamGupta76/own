package com.connect.Auth.repository;

import com.connect.Auth.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<Admin> findByEmailAndActiveTrue(String email);
    
    List<Admin> findByOrganizationIdIsNull();
}

