package com.connect.File.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * File Metadata entity
 * Stores file information and metadata (OneDrive/SharePoint-like abstraction)
 * Actual file content stored on filesystem
 */
@Entity
@Table(name = "file_metadata", indexes = {
    @Index(name = "idx_channel", columnList = "channel_id"),
    @Index(name = "idx_organization", columnList = "organization_id"),
    @Index(name = "idx_uploaded_by", columnList = "uploaded_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private Long size; // File size in bytes
    
    @Column(name = "content_type", nullable = false)
    private String contentType; // MIME type
    
    @Column(name = "file_path", nullable = false)
    private String filePath; // Path on filesystem
    
    @Column(name = "channel_id")
    private Long channelId; // Nullable: if file is in channel
    
    @Column(name = "chat_message_id")
    private Long chatMessageId; // Nullable: if file is attached to chat message
    
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy; // User ID who uploaded
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(nullable = false)
    private Integer version = 1; // File version number
    
    @Column(name = "locked_by")
    private Long lockedBy; // User ID who locked the file (nullable)
    
    @Column(name = "locked_at")
    private LocalDateTime lockedAt; // When file was locked
    
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

