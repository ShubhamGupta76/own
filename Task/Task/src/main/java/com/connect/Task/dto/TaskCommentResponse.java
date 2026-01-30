package com.connect.Task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task comment response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentResponse {
    
    private String id;
    private String taskId;
    private String createdBy;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

