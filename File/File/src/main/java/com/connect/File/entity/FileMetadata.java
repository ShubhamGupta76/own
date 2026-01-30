package com.connect.File.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * File Metadata entity
 * Stores file information and metadata (OneDrive/SharePoint-like abstraction)
 * Actual file content stored on filesystem
 */
@Document(collection = "file_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    
    @Id
    private String id;
    
    private String filename;
    
    private Long size; // File size in bytes
    
    private String contentType; // MIME type
    
    private String filePath; // Path on filesystem
    
    @Indexed
    private String channelId; // Nullable: if file is in channel
    
    private String chatMessageId; // Nullable: if file is attached to chat message
    
    @Indexed
    private String uploadedBy; // User ID who uploaded
    
    @Indexed
    private String organizationId;
    
    @Builder.Default
    private Integer version = 1; // File version number
    
    private String lockedBy; // User ID who locked the file (nullable)
    
    private LocalDateTime lockedAt; // When file was locked
    
    @CreatedDate
    private LocalDateTime uploadedAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

