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
    private Long taskId;
    private String taskTitle;
    private Long assignedTo; // For TASK_ASSIGNED
    private Long assignedBy; // For TASK_ASSIGNED
    private String oldStatus; // For TASK_STATUS_CHANGED
    private String newStatus; // For TASK_STATUS_CHANGED
    private Long commentedBy; // For TASK_COMMENTED
    private Long organizationId;
    private Long channelId;
    private Long teamId;
    private LocalDateTime timestamp;
}

