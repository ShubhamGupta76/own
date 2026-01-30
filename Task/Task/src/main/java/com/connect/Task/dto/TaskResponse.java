package com.connect.Task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for task response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private String id;
    private String title;
    private String description;
    private String organizationId;
    private String teamId;
    private String channelId;
    private String createdBy;
    private String assignedTo;
    private String taskType;
    private String status;
    private String priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TaskCommentResponse> comments;
}

