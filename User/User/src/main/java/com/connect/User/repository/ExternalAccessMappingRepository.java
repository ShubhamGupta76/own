package com.connect.User.repository;

import com.connect.User.entity.ExternalAccessMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExternalAccessMapping entity
 */
@Repository
public interface ExternalAccessMappingRepository extends JpaRepository<ExternalAccessMapping, Long> {
    
    List<ExternalAccessMapping> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    
    List<ExternalAccessMapping> findByTeamIdAndOrganizationId(Long teamId, Long organizationId);
    
    List<ExternalAccessMapping> findByChannelIdAndOrganizationId(Long channelId, Long organizationId);
    
    Optional<ExternalAccessMapping> findByUserIdAndTeamIdAndChannelId(Long userId, Long teamId, Long channelId);
    
    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
    
    boolean existsByUserIdAndChannelId(Long userId, Long channelId);
}

