package com.connect.Notification.service;

import com.connect.Notification.dto.NotificationResponse;
import com.connect.Notification.entity.Notification;
import com.connect.Notification.entity.NotificationConfig;
import com.connect.Notification.repository.NotificationConfigRepository;
import com.connect.Notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for notification operations
 * Handles notifications, activity feed, and real-time delivery via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationConfigRepository notificationConfigRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Get notifications for organization (admin only - legacy method)
     */
    public List<Notification> getNotifications(String organizationId) {
        return notificationRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Get notifications for a user
     */
    public List<NotificationResponse> getNotifications(String userId, String organizationId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<NotificationResponse> getUnreadNotifications(String userId, String organizationId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread notification count
     */
    public long getUnreadCount(String userId, String organizationId) {
        return notificationRepository.countByUserIdAndReadFalseAndOrganizationId(userId, organizationId);
    }
    
    /**
     * Mark notification as read
     */
    public NotificationResponse markAsRead(String notificationId, String userId, String organizationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Verify notification belongs to user and organization
        if (!notification.getUserId().equals(userId) || !notification.getOrganizationId().equals(organizationId)) {
            throw new RuntimeException("Access denied: Notification does not belong to you");
        }
        
        notification.setRead(true);
        notification = notificationRepository.save(notification);
        
        return mapToResponse(notification);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(String userId, String organizationId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Create and send a notification
     * Used by other services to create notifications
     */
    public NotificationResponse createNotification(String userId, String organizationId, 
                                                   Notification.NotificationType type, 
                                                   String title, String message) {
        return createAndSendNotification(userId, organizationId, type, title, message, 
                null, null, null);
    }
    
    /**
     * Create and send a notification with source entity information
     * Used by Kafka event consumers
     */
    public NotificationResponse createAndSendNotification(String userId, String organizationId,
                                                         Notification.NotificationType type, String title, String message,
                                                         Notification.TargetEntityType targetEntityType, String targetEntityId, String targetEntityName) {
        // Check if notification type is enabled
        NotificationConfig config = notificationConfigRepository
                .findByOrganizationIdAndNotificationType(organizationId, type)
                .orElse(NotificationConfig.builder()
                        .organizationId(organizationId)
                        .notificationType(type)
                        .enabled(true)
                        .build());
        
        if (!config.getEnabled()) {
            log.debug("Notification type {} is disabled for organization {}", type, organizationId);
            return null; // Don't create notification if type is disabled
        }
        
        // Create notification
        Notification notification = Notification.builder()
                .userId(userId)
                .organizationId(organizationId)
                .type(type)
                .title(title)
                .message(message)
                .sourceId(targetEntityId) // Store source entity ID
                .read(false)
                .enabled(true)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket (only if userId is not null)
        if (userId != null) {
            NotificationResponse response = mapToResponse(notification);
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, response);
            log.info("Sent notification to user {}: {}", userId, title);
        }
        
        return mapToResponse(notification);
    }
    
    /**
     * Create mention notification (@user, @channel, @team)
     */
    public void createMentionNotification(String mentionedUserId, String organizationId, 
                                         String mentionerName, String context) {
        String title = "You were mentioned";
        String message = mentionerName + " mentioned you: " + context;
        
        createNotification(mentionedUserId, organizationId, Notification.NotificationType.MENTION, title, message);
    }
    
    /**
     * Create task notification
     */
    public void createTaskNotification(String userId, String organizationId, String event, String taskTitle) {
        String title = "Task Update";
        String message = "Task '" + taskTitle + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.TASK, title, message);
    }
    
    /**
     * Create file notification
     */
    public void createFileNotification(String userId, String organizationId, String event, String fileName) {
        String title = "File Update";
        String message = "File '" + fileName + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.FILE, title, message);
    }
    
    /**
     * Create meeting notification
     */
    public void createMeetingNotification(String userId, String organizationId, String event, String meetingTitle) {
        String title = "Meeting Update";
        String message = "Meeting '" + meetingTitle + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.MEETING, title, message);
    }
    
    /**
     * Get activity feed for a user
     * Returns recent notifications and activities
     */
    public List<NotificationResponse> getActivityFeed(String userId, String organizationId, int limit) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        return notifications.stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update notification config
     */
    public NotificationConfig updateNotificationConfig(String organizationId, Notification.NotificationType type, Boolean enabled) {
        NotificationConfig config = notificationConfigRepository.findByOrganizationIdAndNotificationType(organizationId, type)
                .orElseGet(() -> NotificationConfig.builder()
                        .organizationId(organizationId)
                        .notificationType(type)
                        .enabled(true)
                        .build());
        
        config.setEnabled(enabled);
        return notificationConfigRepository.save(config);
    }
    
    /**
     * Get notification configs
     */
    public List<NotificationConfig> getNotificationConfigs(String organizationId) {
        return notificationConfigRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Map Notification entity to NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .organizationId(notification.getOrganizationId())
                .userId(notification.getUserId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

