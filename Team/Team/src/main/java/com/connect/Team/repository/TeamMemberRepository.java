package com.connect.Team.repository;

import com.connect.Team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TeamMember entity
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    /**
     * Find all members of a team
     */
    List<TeamMember> findByTeamId(Long teamId);
    
    /**
     * Find all teams a user belongs to
     */
    List<TeamMember> findByUserId(Long userId);
    
    /**
     * Find members by organization
     */
    List<TeamMember> findByOrganizationId(Long organizationId);
    
    /**
     * Find team member by team ID and user ID
     */
    java.util.Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
    
    /**
     * Check if user is member of team
     */
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}

