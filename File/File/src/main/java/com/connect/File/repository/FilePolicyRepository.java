package com.connect.File.repository;

import com.connect.File.entity.FilePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilePolicyRepository extends JpaRepository<FilePolicy, Long> {
    Optional<FilePolicy> findByOrganizationId(Long organizationId);
}

