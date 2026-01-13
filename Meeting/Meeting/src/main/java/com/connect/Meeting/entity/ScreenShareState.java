package com.connect.Meeting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Screen Share State entity
 * Tracks screen sharing status in meetings
 */
@Entity
@Table(name = "screen_share_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenShareState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;
    
    @Column(name = "started_by", nullable = false)
    private Long startedBy; // User ID who started screen sharing
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}

