package com.connect.File.repository;

import com.connect.File.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FileMetadata entity
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    List<FileMetadata> findByChannelIdAndOrganizationId(Long channelId, Long organizationId);
    
    List<FileMetadata> findByChatMessageIdAndOrganizationId(Long chatMessageId, Long organizationId);
    
    List<FileMetadata> findByOrganizationId(Long organizationId);
    
    Optional<FileMetadata> findByIdAndOrganizationId(Long id, Long organizationId);
    
    List<FileMetadata> findByUploadedByAndOrganizationId(Long uploadedBy, Long organizationId);
}

