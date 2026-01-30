package com.connect.File.repository;

import com.connect.File.entity.FilePolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilePolicyRepository extends MongoRepository<FilePolicy, String> {
    Optional<FilePolicy> findByOrganizationId(String organizationId);
}

