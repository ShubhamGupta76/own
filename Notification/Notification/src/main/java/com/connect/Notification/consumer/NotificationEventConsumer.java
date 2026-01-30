package com.connect.Notification.consumer;

import com.connect.Notification.entity.Notification;
import com.connect.Notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka Event Consumer for Notification Service
 * Consumes events from various topics and creates notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {
    
    private final NotificationService notificationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Consume chat events
     */
    @KafkaListener(topics = "chat-events", groupId = "notification-service-group")
    public void consumeChatEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            String eventType = (String) event.get("eventType");
            String organizationId = getStringValue(event, "organizationId");
            
            if ("MESSAGE_SENT".equals(eventType)) {
                handleMessageSentEvent(event, organizationId);
            }
            
            acknowledgment.acknowledge();
            log.info("Processed chat event: {}", eventType);
        } catch (Exception e) {
            log.error("Error processing chat event: {}", e.getMessage(), e);
            try {
                sendToDeadLetterQueue("chat-events-dlq", event, e);
            } catch (Exception dlqException) {
                log.error("Failed to send event to DLQ: {}", dlqException.getMessage(), dlqException);
            }
        }
    }
    
    /**
     * Consume meeting events
     */
    @KafkaListener(topics = "meeting-events", groupId = "notification-service-group")
    public void consumeMeetingEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            String eventType = (String) event.get("eventType");
            String organizationId = getStringValue(event, "organizationId");
            
            switch (eventType) {
                case "MEETING_CREATED":
                    handleMeetingCreatedEvent(event, organizationId);
                    break;
                case "USER_JOINED":
                    handleUserJoinedEvent(event, organizationId);
                    break;
                case "USER_LEFT":
                    // Optional: Notify meeting organizer
                    break;
                case "RECORDING_STARTED":
                    handleRecordingStartedEvent(event, organizationId);
                    break;
                case "RECORDING_STOPPED":
                    handleRecordingStoppedEvent(event, organizationId);
                    break;
            }
            
            acknowledgment.acknowledge();
            log.info("Processed meeting event: {}", eventType);
        } catch (Exception e) {
            log.error("Error processing meeting event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Consume file events
     */
    @KafkaListener(topics = "file-events", groupId = "notification-service-group")
    public void consumeFileEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            String eventType = (String) event.get("eventType");
            String organizationId = getStringValue(event, "organizationId");
            
            if ("FILE_UPLOADED".equals(eventType)) {
                handleFileUploadedEvent(event, organizationId);
            }
            
            acknowledgment.acknowledge();
            log.info("Processed file event: {}", eventType);
        } catch (Exception e) {
            log.error("Error processing file event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Consume task events
     */
    @KafkaListener(topics = "task-events", groupId = "notification-service-group")
    public void consumeTaskEvent(Map<String, Object> event, Acknowledgment acknowledgment) {
        try {
            String eventType = (String) event.get("eventType");
            String organizationId = getStringValue(event, "organizationId");
            
            switch (eventType) {
                case "TASK_ASSIGNED":
                    handleTaskAssignedEvent(event, organizationId);
                    break;
                case "TASK_STATUS_CHANGED":
                    handleTaskStatusChangedEvent(event, organizationId);
                    break;
                case "TASK_COMMENTED":
                    handleTaskCommentedEvent(event, organizationId);
                    break;
            }
            
            acknowledgment.acknowledge();
            log.info("Processed task event: {}", eventType);
        } catch (Exception e) {
            log.error("Error processing task event: {}", e.getMessage(), e);
        }
    }
    
    // ========== Event Handlers ==========
    
    private void handleMessageSentEvent(Map<String, Object> event, String organizationId) {
        String channelId = getStringValue(event, "channelId");
        String messageContent = (String) event.get("messageContent");
        
        // Get mentioned user IDs
        @SuppressWarnings("unchecked")
        List<Object> mentionedUserIds = (List<Object>) event.get("mentionedUserIds");
        
        if (mentionedUserIds != null && !mentionedUserIds.isEmpty()) {
            // Create notifications for mentioned users
            for (Object mentionedUserId : mentionedUserIds) {
                notificationService.createAndSendNotification(
                        mentionedUserId != null ? mentionedUserId.toString() : null,
                        organizationId,
                        Notification.NotificationType.MENTION,
                        "You were mentioned",
                        String.format("You were mentioned in a message: %s", 
                                messageContent != null && messageContent.length() > 50 
                                        ? messageContent.substring(0, 50) + "..." 
                                        : messageContent),
                        Notification.TargetEntityType.CHANNEL,
                        channelId,
                        null
                );
            }
        }
    }
    
    private void handleMeetingCreatedEvent(Map<String, Object> event, String organizationId) {
        String meetingId = getStringValue(event, "meetingId");
        String meetingTitle = (String) event.get("meetingTitle");
        @SuppressWarnings("unchecked")
        List<Object> participantIds = (List<Object>) event.get("participantIds");
        
        if (participantIds != null && !participantIds.isEmpty()) {
            // Notify all participants (except the creator who already knows)
            for (Object participantId : participantIds) {
                notificationService.createAndSendNotification(
                        participantId != null ? participantId.toString() : null,
                        organizationId,
                        Notification.NotificationType.MEETING,
                        "Meeting Created",
                        String.format("You are invited to meeting: %s", meetingTitle),
                        Notification.TargetEntityType.MEETING,
                        meetingId,
                        meetingTitle
                );
            }
        }
    }
    
    private void handleUserJoinedEvent(Map<String, Object> event, String organizationId) {
        // Optional: Notify meeting organizer or other participants
        // For now, we'll skip this to avoid notification spam
    }
    
    private void handleRecordingStartedEvent(Map<String, Object> event, String organizationId) {
        String meetingId = getStringValue(event, "meetingId");
        
        // Notify all meeting participants that recording has started
        // Note: In production, fetch participants from Meeting Service
        notificationService.createAndSendNotification(
                null, // System notification
                organizationId,
                Notification.NotificationType.MEETING,
                "Recording Started",
                "Meeting recording has started",
                Notification.TargetEntityType.MEETING,
                meetingId,
                null
        );
    }
    
    private void handleRecordingStoppedEvent(Map<String, Object> event, String organizationId) {
        String meetingId = getStringValue(event, "meetingId");
        String recordingUrl = (String) event.get("recordingUrl");
        
        // Notify all meeting participants that recording is available
        notificationService.createAndSendNotification(
                null, // System notification
                organizationId,
                Notification.NotificationType.MEETING,
                "Recording Available",
                String.format("Meeting recording is now available: %s", recordingUrl),
                Notification.TargetEntityType.MEETING,
                meetingId,
                null
        );
    }
    
    private void handleFileUploadedEvent(Map<String, Object> event, String organizationId) {
        String fileId = getStringValue(event, "fileId");
        String filename = (String) event.get("filename");
        
        // Notify channel members about new file
        // Note: In production, fetch channel members from Channel Service
        notificationService.createAndSendNotification(
                null, // Channel-wide notification
                organizationId,
                Notification.NotificationType.FILE,
                "File Uploaded",
                String.format("New file uploaded: %s", filename),
                Notification.TargetEntityType.FILE,
                fileId,
                filename
        );
    }
    
    private void handleTaskAssignedEvent(Map<String, Object> event, String organizationId) {
        String taskId = getStringValue(event, "taskId");
        String taskTitle = (String) event.get("taskTitle");
        String assignedTo = getStringValue(event, "assignedTo");
        
        // Notify the assigned user
        notificationService.createAndSendNotification(
                assignedTo,
                organizationId,
                Notification.NotificationType.TASK,
                "Task Assigned",
                String.format("You have been assigned a task: %s", taskTitle),
                Notification.TargetEntityType.TASK,
                taskId,
                taskTitle
        );
    }
    
    private void handleTaskStatusChangedEvent(Map<String, Object> event, String organizationId) {
        String taskId = getStringValue(event, "taskId");
        String taskTitle = (String) event.get("taskTitle");
        String newStatus = (String) event.get("newStatus");
        
        // Notify task creator and assignee about status change
        // Note: In production, fetch task details from Task Service
        notificationService.createAndSendNotification(
                null, // Task-related notification
                organizationId,
                Notification.NotificationType.TASK,
                "Task Status Changed",
                String.format("Task '%s' status changed to: %s", taskTitle, newStatus),
                Notification.TargetEntityType.TASK,
                taskId,
                taskTitle
        );
    }
    
    private void handleTaskCommentedEvent(Map<String, Object> event, String organizationId) {
        String taskId = getStringValue(event, "taskId");
        String taskTitle = (String) event.get("taskTitle");
        
        // Notify task assignee and creator about new comment
        // Note: In production, fetch task details from Task Service
        notificationService.createAndSendNotification(
                null, // Task-related notification
                organizationId,
                Notification.NotificationType.TASK,
                "New Comment",
                String.format("New comment added to task: %s", taskTitle),
                Notification.TargetEntityType.TASK,
                taskId,
                taskTitle
        );
    }
    
    private String getStringValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null) return null;
        return value.toString();
    }
    
    private Long getLongValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
    
    private void sendToDeadLetterQueue(String dlqTopic, Map<String, Object> event, Exception error) {
        try {
            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("originalEvent", event);
            dlqMessage.put("error", error.getMessage());
            dlqMessage.put("timestamp", System.currentTimeMillis());
            dlqMessage.put("service", "notification-service");
            
            String organizationId = getStringValue(event, "organizationId");
            String key = organizationId != null ? organizationId : "unknown";
            
            kafkaTemplate.send(dlqTopic, key, dlqMessage);
            log.info("Sent failed event to DLQ topic: {}", dlqTopic);
        } catch (Exception e) {
            log.error("Failed to send event to DLQ topic {}: {}", dlqTopic, e.getMessage(), e);
        }
    }
}

