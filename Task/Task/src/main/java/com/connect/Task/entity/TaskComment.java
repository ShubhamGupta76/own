package com.connect.Task.entity;

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
 * Task Comment entity
 * Comments on tasks for collaboration
 */
@Document(collection = "task_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskComment {
    
    @Id
    private String id;
    
    @Indexed
    private String taskId;
    
    private String createdBy; // User ID who created the comment
    
    @Indexed
    private String organizationId;
    
    private String content; // Comment content
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
