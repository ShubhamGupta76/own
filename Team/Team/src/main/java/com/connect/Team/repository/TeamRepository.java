package com.connect.Team.repository;

import com.connect.Team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Team entity
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    /**
     * Find all teams in an organization
     */
    List<Team> findByOrganizationId(Long organizationId);
    
    /**
     * Find active teams in an organization
     */
    List<Team> findByOrganizationIdAndActiveTrue(Long organizationId);
}

