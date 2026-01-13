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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long organizationId) {
        return notificationRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Get notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, Long organizationId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId, Long organizationId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId, Long organizationId) {
        return notificationRepository.countByUserIdAndReadFalseAndOrganizationId(userId, organizationId);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId, Long organizationId) {
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
    @Transactional
    public void markAllAsRead(Long userId, Long organizationId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(userId, organizationId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Create and send a notification
     * Used by other services to create notifications
     */
    @Transactional
    public NotificationResponse createNotification(Long userId, Long organizationId, 
                                                   Notification.NotificationType type, 
                                                   String title, String message) {
        return createAndSendNotification(userId, organizationId, type, title, message, 
                null, null, null);
    }
    
    /**
     * Create and send a notification with source entity information
     * Used by Kafka event consumers
     */
    @Transactional
    public NotificationResponse createAndSendNotification(Long userId, Long organizationId,
                                                         Notification.NotificationType type, String title, String message,
                                                         Notification.TargetEntityType targetEntityType, Long targetEntityId, String targetEntityName) {
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
    @Transactional
    public void createMentionNotification(Long mentionedUserId, Long organizationId, 
                                         String mentionerName, String context) {
        String title = "You were mentioned";
        String message = mentionerName + " mentioned you: " + context;
        
        createNotification(mentionedUserId, organizationId, Notification.NotificationType.MENTION, title, message);
    }
    
    /**
     * Create task notification
     */
    @Transactional
    public void createTaskNotification(Long userId, Long organizationId, String event, String taskTitle) {
        String title = "Task Update";
        String message = "Task '" + taskTitle + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.TASK, title, message);
    }
    
    /**
     * Create file notification
     */
    @Transactional
    public void createFileNotification(Long userId, Long organizationId, String event, String fileName) {
        String title = "File Update";
        String message = "File '" + fileName + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.FILE, title, message);
    }
    
    /**
     * Create meeting notification
     */
    @Transactional
    public void createMeetingNotification(Long userId, Long organizationId, String event, String meetingTitle) {
        String title = "Meeting Update";
        String message = "Meeting '" + meetingTitle + "' - " + event;
        
        createNotification(userId, organizationId, Notification.NotificationType.MEETING, title, message);
    }
    
    /**
     * Get activity feed for a user
     * Returns recent notifications and activities
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getActivityFeed(Long userId, Long organizationId, int limit) {
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
    @Transactional
    public NotificationConfig updateNotificationConfig(Long organizationId, Notification.NotificationType type, Boolean enabled) {
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
    @Transactional(readOnly = true)
    public List<NotificationConfig> getNotificationConfigs(Long organizationId) {
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

