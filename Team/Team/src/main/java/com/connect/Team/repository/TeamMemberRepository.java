package com.connect.Team.repository;

import com.connect.Team.entity.TeamMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TeamMember entity
 */
@Repository
public interface TeamMemberRepository extends MongoRepository<TeamMember, String> {
    
    /**
     * Find all members of a team
     */
    List<TeamMember> findByTeamId(String teamId);
    
    /**
     * Find all teams a user belongs to
     */
    List<TeamMember> findByUserId(String userId);
    
    /**
     * Find members by organization
     */
    List<TeamMember> findByOrganizationId(String organizationId);
    
    /**
     * Find team member by team ID and user ID
     */
    java.util.Optional<TeamMember> findByTeamIdAndUserId(String teamId, String userId);
    
    /**
     * Check if user is member of team
     */
    boolean existsByTeamIdAndUserId(String teamId, String userId);
}

