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
 * Meeting Note entity
 * Stores notes taken during meetings
 */
@Document(collection = "meeting_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingNote {
    
    @Id
    private String id;
    
    @Indexed
    private String meetingId;
    
    private String createdBy; // User ID who created the note
    
    @Indexed
    private String organizationId;
    
    private String content; // Note content
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

