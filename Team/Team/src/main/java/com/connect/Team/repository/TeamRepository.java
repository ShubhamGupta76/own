package com.connect.Team.repository;

import com.connect.Team.entity.Team;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Team entity
 */
@Repository
public interface TeamRepository extends MongoRepository<Team, String> {
    
    /**
     * Find all teams in an organization
     */
    List<Team> findByOrganizationId(String organizationId);
    
    /**
     * Find active teams in an organization
     */
    List<Team> findByOrganizationIdAndActiveTrue(String organizationId);
}

