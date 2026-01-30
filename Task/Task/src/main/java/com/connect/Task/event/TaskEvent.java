package com.connect.Task.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task Event DTO for Kafka
 * Published for task operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    
    private String eventType; // TASK_ASSIGNED, TASK_STATUS_CHANGED, TASK_COMMENTED
    private String taskId;
    private String taskTitle;
    private String assignedTo; // For TASK_ASSIGNED
    private String assignedBy; // For TASK_ASSIGNED
    private String oldStatus; // For TASK_STATUS_CHANGED
    private String newStatus; // For TASK_STATUS_CHANGED
    private String commentedBy; // For TASK_COMMENTED
    private String organizationId;
    private String channelId;
    private String teamId;
    private LocalDateTime timestamp;
}

