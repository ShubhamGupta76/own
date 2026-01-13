package com.connect.Chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for chat room response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    
    private Long id;
    private String roomType;
    private Long roomId;
    private Long user1Id;
    private Long user2Id;
    private Long organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

