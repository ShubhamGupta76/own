package com.connect.Chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

/**
 * ChatRoom entity
 * Represents a chat room (channel, team, or direct chat)
 */
@Document(collection = "chat_rooms")
@CompoundIndex(name = "room_type_room_id_org_idx", def = "{'roomType': 1, 'roomId': 1, 'organizationId': 1}", unique = true, partialFilter = "{'roomId': {$exists: true}}")
@CompoundIndex(name = "direct_chat_users_idx", def = "{'roomType': 1, 'user1Id': 1, 'user2Id': 1, 'organizationId': 1}", unique = true, partialFilter = "{'roomType': 'DIRECT'}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    
    @Id
    private String id;
    
    private RoomType roomType; // CHANNEL, TEAM, DIRECT
    
    private String roomId; // Channel ID, Team ID, or null for direct chat
    
    private String user1Id; // For DIRECT chat: first user ID
    
    private String user2Id; // For DIRECT chat: second user ID
    
    @Indexed
    private String organizationId;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum RoomType {
        CHANNEL,  // Channel chat
        TEAM,     // Team chat
        DIRECT    // Direct 1-to-1 chat
    }
}

