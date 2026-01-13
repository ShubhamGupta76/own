package com.connect.Meeting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Meeting entity
 * Represents both instant calls and scheduled meetings
 */
@Entity
@Table(name = "meetings", indexes = {
    @Index(name = "idx_organization", columnList = "organization_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // User ID who created the meeting
    
    @Column(name = "meeting_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MeetingType meetingType; // INSTANT or SCHEDULED
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // For scheduled meetings
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // For scheduled meetings
    
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime; // When meeting actually started
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime; // When meeting actually ended
    
    @Column(name = "team_id")
    private Long teamId; // Optional: if meeting is for a team
    
    @Column(name = "channel_id")
    private Long channelId; // Optional: if meeting is for a channel
    
    @Column(name = "meeting_url")
    private String meetingUrl; // Video conferencing URL (e.g., Zoom, Teams link)
    
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
    
    public enum MeetingType {
        INSTANT,  // Instant call (no scheduling)
        SCHEDULED // Scheduled meeting
    }
    
    public enum MeetingStatus {
        SCHEDULED, // Meeting is scheduled
        LIVE,      // Meeting is currently active
        ENDED      // Meeting has ended
    }
}

