package com.connect.Chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Message entity
 * Stores all chat messages (channel, team, direct)
 */
@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    private String id;
    
    @Indexed
    private String chatRoomId; // Reference to ChatRoom (channel, team, or direct chat)
    
    @Indexed
    private String senderId; // User ID who sent the message
    
    private SenderRole senderRole; // Role of sender (ADMIN, MANAGER, EMPLOYEE)
    
    @Indexed
    private String organizationId;
    
    private String content; // Message content
    
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    private String fileUrl; // URL if message type is FILE
    
    private String fileId; // File ID if message type is FILE
    
    private String linkUrl; // URL if message type is LINK
    
    private String emojiCode; // Emoji code if message type is EMOJI
    
    private String gifUrl; // GIF URL if message type is GIF
    
    private String metadata; // JSON metadata for additional information
    
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
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

