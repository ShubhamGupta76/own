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
    
    private Long id;
    private String filename;
    private Long size;
    private String contentType;
    private Long channelId;
    private Long chatMessageId;
    private Long uploadedBy;
    private Long organizationId;
    private Integer version;
    private Long lockedBy;
    private LocalDateTime lockedAt;
    private Boolean isLocked;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
}

