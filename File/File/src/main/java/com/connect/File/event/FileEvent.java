package com.connect.File.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * File Event DTO for Kafka
 * Published for file operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {
    
    private String eventType; // FILE_UPLOADED, FILE_DELETED
    private Long fileId;
    private String filename;
    private Long channelId;
    private Long uploadedBy; // User ID who uploaded/deleted
    private Long organizationId;
    private Long fileSize;
    private String contentType;
    private LocalDateTime timestamp;
}

