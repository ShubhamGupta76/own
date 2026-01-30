package com.connect.User.repository;

import com.connect.User.entity.ExternalAccessMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExternalAccessMapping entity
 */
@Repository
public interface ExternalAccessMappingRepository extends MongoRepository<ExternalAccessMapping, String> {
    
    List<ExternalAccessMapping> findByUserIdAndOrganizationId(String userId, String organizationId);
    
    List<ExternalAccessMapping> findByTeamIdAndOrganizationId(String teamId, String organizationId);
    
    List<ExternalAccessMapping> findByChannelIdAndOrganizationId(String channelId, String organizationId);
    
    Optional<ExternalAccessMapping> findByUserIdAndTeamIdAndChannelId(String userId, String teamId, String channelId);
    
    boolean existsByUserIdAndTeamId(String userId, String teamId);
    
    boolean existsByUserIdAndChannelId(String userId, String channelId);
}

