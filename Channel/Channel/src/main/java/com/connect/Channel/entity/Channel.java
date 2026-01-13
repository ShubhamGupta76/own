package com.connect.Channel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Channel entity
 * Channels belong to teams
 */
@Entity
@Table(name = "channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "team_id", nullable = false)
    private Long teamId;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType type = ChannelType.STANDARD;
    
    @Column(name = "created_by")
    private Long createdBy; // User ID who created the channel
    
    @Column(name = "chat_enabled", nullable = false)
    private Boolean chatEnabled = true;
    
    @Column(name = "file_enabled", nullable = false)
    private Boolean fileEnabled = true;
    
    @Column(name = "meeting_enabled", nullable = false)
    private Boolean meetingEnabled = true;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ChannelType {
        STANDARD,  // Standard channel (default)
        PRIVATE    // Private channel
    }
}

