package com.connect.Chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message entity
 * Stores all chat messages (channel, team, direct)
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_chat_room", columnList = "chat_room_id"),
    @Index(name = "idx_organization", columnList = "organization_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId; // Reference to ChatRoom (channel, team, or direct chat)
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId; // User ID who sent the message
    
    @Column(name = "sender_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private SenderRole senderRole; // Role of sender (ADMIN, MANAGER, EMPLOYEE)
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Message content
    
    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "file_url")
    private String fileUrl; // URL if message type is FILE
    
    @Column(name = "file_id")
    private Long fileId; // File ID if message type is FILE
    
    @Column(name = "link_url")
    private String linkUrl; // URL if message type is LINK
    
    @Column(name = "emoji_code")
    private String emojiCode; // Emoji code if message type is EMOJI
    
    @Column(name = "gif_url")
    private String gifUrl; // GIF URL if message type is GIF
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata for additional information
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MessageType {
        TEXT,
        FILE,
        LINK,
        EMOJI,
        GIF,
        SYSTEM
    }
    
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }
    
    public enum SenderRole {
        ADMIN,
        MANAGER,
        EMPLOYEE
    }
}

