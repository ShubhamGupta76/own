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
    
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderRole;
    private Long organizationId;
    private String content;
    private String messageType;
    private String fileUrl;
    private Long fileId;
    private String linkUrl;
    private String emojiCode;
    private String gifUrl;
    private String metadata;
    private String status;
    private LocalDateTime createdAt;
}

