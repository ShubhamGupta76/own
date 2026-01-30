package com.connect.Meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for meeting note response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingNoteResponse {
    
    private String id;
    private String meetingId;
    private String createdBy;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

