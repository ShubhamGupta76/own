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
    
    private String id;
    private String meetingId;
    private String startedBy;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Boolean isActive;
}

