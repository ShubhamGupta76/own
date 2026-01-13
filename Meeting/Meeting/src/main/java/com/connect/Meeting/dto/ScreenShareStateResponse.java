package com.connect.Meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for screen share state response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenShareStateResponse {
    
    private Long id;
    private Long meetingId;
    private Long startedBy;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Boolean isActive;
}

