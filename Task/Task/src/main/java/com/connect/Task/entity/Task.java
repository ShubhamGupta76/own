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
 * Task entity
 * Jira-like task management linked to teams and channels
 */
@Document(collection = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    private String id;
    
    private String title;
    
    private String description;
    
    @Indexed
    private String organizationId;
    
    @Indexed
    private String teamId; // Optional: if task is for a team
    
    @Indexed
    private String channelId; // Optional: if task is for a channel
    
    private String createdBy; // User ID who created the task
    
    @Indexed
    private String assignedTo; // User ID assigned to the task (nullable)
    
    @Indexed
    @Builder.Default
    private TaskType taskType = TaskType.TASK;
    
    @Indexed
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;
    
    private Priority priority = Priority.MEDIUM;
    
    private LocalDateTime dueDate;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum TaskType {
        TASK,
        BUG,
        STORY
    }
    
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        BLOCKED,
        DONE
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}

