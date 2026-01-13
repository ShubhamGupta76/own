package com.connect.Task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task entity
 * Jira-like task management linked to teams and channels
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_team", columnList = "team_id"),
    @Index(name = "idx_channel", columnList = "channel_id"),
    @Index(name = "idx_organization", columnList = "organization_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "team_id")
    private Long teamId; // Optional: if task is for a team
    
    @Column(name = "channel_id")
    private Long channelId; // Optional: if task is for a channel
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // User ID who created the task
    
    @Column(name = "assigned_to")
    private Long assignedTo; // User ID assigned to the task (nullable)
    
    @Column(name = "task_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType taskType = TaskType.TASK;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
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

