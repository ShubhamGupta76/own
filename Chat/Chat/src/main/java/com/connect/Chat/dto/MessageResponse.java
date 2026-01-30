package com.connect.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for message response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private String id;
    private String chatRoomId;
    private String senderId;
    private String senderRole;
    private String organizationId;
    private String content;
    private String messageType;
    private String fileUrl;
    private String fileId;
    private String linkUrl;
    private String emojiCode;
    private String gifUrl;
    private String metadata;
    private String status;
    private LocalDateTime createdAt;
}

