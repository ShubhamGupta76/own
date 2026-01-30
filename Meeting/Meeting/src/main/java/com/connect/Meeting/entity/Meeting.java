package com.connect.Meeting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Meeting entity
 * Represents both instant calls and scheduled meetings
 */
@Document(collection = "meetings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    
    @Id
    private String id;
    
    private String title;
    
    private String description;
    
    @Indexed
    private String organizationId;
    
    private String createdBy; // User ID who created the meeting
    
    private MeetingType meetingType; // INSTANT or SCHEDULED
    
    @Indexed
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    
    @Indexed
    private LocalDateTime startTime; // For scheduled meetings
    
    private LocalDateTime endTime; // For scheduled meetings
    
    private LocalDateTime actualStartTime; // When meeting actually started
    
    private LocalDateTime actualEndTime; // When meeting actually ended
    
    private String teamId; // Optional: if meeting is for a team
    
    private String channelId; // Optional: if meeting is for a channel
    
    private String meetingUrl; // Video conferencing URL (e.g., Zoom, Teams link)
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
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

