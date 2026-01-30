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

@Document(collection = "file_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilePolicy {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String organizationId;
    
    @Builder.Default
    private Boolean enabled = true;
    
    private Integer maxFileSizeMb;
    
    private String allowedFileTypes; // Comma-separated
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

