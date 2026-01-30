package com.connect.Notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private String organizationId;
    
    @Indexed
    private String userId; // null for system-wide notifications
    
    private NotificationType type;
    
    private String title;
    
    private String message;
    
    private String sourceId; // ID of the source entity (messageId, taskId, fileId, meetingId)
    
    @Indexed
    @Builder.Default
    private Boolean read = false;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        SYSTEM,
        MENTION,      // @user, @channel, @team mentions
        TASK,         // Task assignment, status change
        FILE,         // File upload, share
        MEETING,      // Meeting events
        MESSAGE,      // Missed messages, replies
        ACTIVITY      // General activity feed
    }
    
    public enum TargetEntityType {
        USER,
        TEAM,
        CHANNEL,
        TASK,
        FILE,
        MEETING
    }
}

