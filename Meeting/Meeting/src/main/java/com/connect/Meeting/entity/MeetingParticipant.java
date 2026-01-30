package com.connect.Meeting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

/**
 * Meeting Participant entity
 * Tracks who joined/left meetings
 */
@Document(collection = "meeting_participants")
@CompoundIndex(name = "meeting_user_idx", def = "{'meetingId': 1, 'userId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingParticipant {
    
    @Id
    private String id;
    
    @Indexed
    private String meetingId;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String organizationId;
    
    @CreatedDate
    private LocalDateTime joinedAt;
    
    private LocalDateTime leftAt;
    
    @Builder.Default
    private Boolean isActive = true;
}

