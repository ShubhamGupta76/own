package com.connect.Chat.dto;

import com.connect.Chat.entity.Message;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending a message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    private String content; // Required for TEXT, optional for other types
    
    @NotNull(message = "Chat room ID is required")
    private Long chatRoomId;
    
    private Message.MessageType messageType = Message.MessageType.TEXT;
    
    // Metadata fields based on message type
    private String fileUrl; // For FILE type messages
    private Long fileId; // For FILE type messages
    private String linkUrl; // For LINK type messages
    private String emojiCode; // For EMOJI type messages
    private String gifUrl; // For GIF type messages
    private String metadata; // Additional JSON metadata
}

