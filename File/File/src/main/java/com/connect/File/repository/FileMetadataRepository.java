package com.connect.File.repository;

import com.connect.File.entity.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FileMetadata entity
 */
@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    
    List<FileMetadata> findByChannelIdAndOrganizationId(String channelId, String organizationId);
    
    List<FileMetadata> findByChatMessageIdAndOrganizationId(String chatMessageId, String organizationId);
    
    List<FileMetadata> findByOrganizationId(String organizationId);
    
    Optional<FileMetadata> findByIdAndOrganizationId(String id, String organizationId);
    
    List<FileMetadata> findByUploadedByAndOrganizationId(String uploadedBy, String organizationId);
}

