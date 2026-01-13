package com.connect.Meeting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Recording State entity
 * Tracks meeting recording status
 */
@Entity
@Table(name = "recording_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingState {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;
    
    @Column(name = "recorded_by", nullable = false)
    private Long recordedBy; // User ID who started recording
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "recording_url")
    private String recordingUrl; // Placeholder for recording URL
    
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

