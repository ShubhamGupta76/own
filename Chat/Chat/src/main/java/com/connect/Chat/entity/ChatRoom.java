package com.connect.Chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ChatRoom entity
 * Represents a chat room (channel, team, or direct chat)
 */
@Entity
@Table(name = "chat_rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_type", "room_id", "organization_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomType roomType; // CHANNEL, TEAM, DIRECT
    
    @Column(name = "room_id", nullable = false)
    private Long roomId; // Channel ID, Team ID, or null for direct chat
    
    @Column(name = "user1_id")
    private Long user1Id; // For DIRECT chat: first user ID
    
    @Column(name = "user2_id")
    private Long user2Id; // For DIRECT chat: second user ID
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum RoomType {
        CHANNEL,  // Channel chat
        TEAM,     // Team chat
        DIRECT    // Direct 1-to-1 chat
    }
}

