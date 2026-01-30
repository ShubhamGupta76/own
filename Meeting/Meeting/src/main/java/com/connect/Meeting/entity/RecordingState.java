package com.connect.Meeting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Recording State entity
 * Tracks meeting recording status
 */
@Document(collection = "recording_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingState {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String meetingId;
    
    private String recordedBy; // User ID who started recording
    
    @CreatedDate
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    private String recordingUrl; // Placeholder for recording URL
    
    @Builder.Default
    private Boolean isActive = true;
}

