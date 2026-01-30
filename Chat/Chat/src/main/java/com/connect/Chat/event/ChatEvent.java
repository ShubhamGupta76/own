package com.connect.Chat.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat Event DTO for Kafka
 * Published when a message is sent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
    
    private String eventType; // MESSAGE_SENT
    private String messageId;
    private String channelId;
    private String chatRoomId;
    private String senderId;
    private String senderRole;
    private List<String> mentionedUserIds; // Users mentioned in the message
    private String organizationId;
    private String messageContent;
    private LocalDateTime timestamp;
}

