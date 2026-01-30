package com.connect.File.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file metadata response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataResponse {
    
    private String id;
    private String filename;
    private Long size;
    private String contentType;
    private String channelId;
    private String chatMessageId;
    private String uploadedBy;
    private String organizationId;
    private Integer version;
    private String lockedBy;
    private LocalDateTime lockedAt;
    private Boolean isLocked;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
}

