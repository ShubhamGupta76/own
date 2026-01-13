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
    private Long messageId;
    private Long channelId;
    private Long chatRoomId;
    private Long senderId;
    private String senderRole;
    private List<Long> mentionedUserIds; // Users mentioned in the message
    private Long organizationId;
    private String messageContent;
    private LocalDateTime timestamp;
}

