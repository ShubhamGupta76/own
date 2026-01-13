package com.connect.Task.service;

import com.connect.Task.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka Event Producer for Task Service
 * Publishes task events to Kafka topic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer {
    
    private static final String TASK_EVENTS_TOPIC = "task-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish TASK_ASSIGNED event
     */
    public void publishTaskAssignedEvent(Long taskId, String taskTitle, Long assignedTo,
                                       Long assignedBy, Long organizationId,
                                       Long channelId, Long teamId) {
        try {
            TaskEvent event = TaskEvent.builder()
                    .eventType("TASK_ASSIGNED")
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .assignedTo(assignedTo)
                    .assignedBy(assignedBy)
                    .organizationId(organizationId)
                    .channelId(channelId)
                    .teamId(teamId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(TASK_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published TASK_ASSIGNED event for taskId: {}, assignedTo: {}", taskId, assignedTo);
        } catch (Exception e) {
            log.error("Failed to publish TASK_ASSIGNED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish TASK_STATUS_CHANGED event
     */
    public void publishTaskStatusChangedEvent(Long taskId, String taskTitle,
                                             String oldStatus, String newStatus,
                                             Long organizationId, Long channelId, Long teamId) {
        try {
            TaskEvent event = TaskEvent.builder()
                    .eventType("TASK_STATUS_CHANGED")
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .organizationId(organizationId)
                    .channelId(channelId)
                    .teamId(teamId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(TASK_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published TASK_STATUS_CHANGED event for taskId: {}, status: {} -> {}", taskId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to publish TASK_STATUS_CHANGED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish TASK_COMMENTED event
     */
    public void publishTaskCommentedEvent(Long taskId, String taskTitle, Long commentedBy,
                                         Long organizationId, Long channelId, Long teamId) {
        try {
            TaskEvent event = TaskEvent.builder()
                    .eventType("TASK_COMMENTED")
                    .taskId(taskId)
                    .taskTitle(taskTitle)
                    .commentedBy(commentedBy)
                    .organizationId(organizationId)
                    .channelId(channelId)
                    .teamId(teamId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(TASK_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published TASK_COMMENTED event for taskId: {}, commentedBy: {}", taskId, commentedBy);
        } catch (Exception e) {
            log.error("Failed to publish TASK_COMMENTED event: {}", e.getMessage(), e);
        }
    }
}

