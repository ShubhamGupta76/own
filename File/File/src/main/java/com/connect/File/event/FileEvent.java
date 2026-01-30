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
    private String fileId;
    private String filename;
    private String channelId;
    private String uploadedBy; // User ID who uploaded/deleted
    private String organizationId;
    private Long fileSize;
    private String contentType;
    private LocalDateTime timestamp;
}

