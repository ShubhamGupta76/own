package com.connect.Meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for meeting participant response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingParticipantResponse {
    
    private Long id;
    private Long meetingId;
    private Long userId;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isActive;
}

