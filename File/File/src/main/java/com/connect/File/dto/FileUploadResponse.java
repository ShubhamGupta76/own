package com.connect.File.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file upload response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private String id;
    private String filename;
    private Long size;
    private String contentType;
    private String channelId;
    private String chatMessageId;
    private String uploadedBy;
    private String organizationId;
    private Integer version;
    private LocalDateTime uploadedAt;
    private String downloadUrl; // URL to download the file
}

