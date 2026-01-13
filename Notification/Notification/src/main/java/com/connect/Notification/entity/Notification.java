package com.connect.Notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "user_id")
    private Long userId; // null for system-wide notifications
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "source_id")
    private Long sourceId; // ID of the source entity (messageId, taskId, fileId, meetingId)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean read = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
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

