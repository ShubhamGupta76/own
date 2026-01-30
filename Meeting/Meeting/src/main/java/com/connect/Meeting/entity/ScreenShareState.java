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
 * Screen Share State entity
 * Tracks screen sharing status in meetings
 */
@Document(collection = "screen_share_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenShareState {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String meetingId;
    
    private String startedBy; // User ID who started screen sharing
    
    @CreatedDate
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    @Builder.Default
    private Boolean isActive = true;
}

