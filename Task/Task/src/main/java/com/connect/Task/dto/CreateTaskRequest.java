package com.connect.Task.dto;

import com.connect.Task.entity.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a task
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private Long teamId;
    
    private Long channelId;
    
    private Long assignedTo;
    
    private String taskType = "TASK"; // TASK, BUG, STORY
    
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    
    private LocalDateTime dueDate;
}

