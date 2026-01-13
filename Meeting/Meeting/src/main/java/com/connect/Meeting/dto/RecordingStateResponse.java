package com.connect.Meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for recording state response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStateResponse {
    
    private Long id;
    private Long meetingId;
    private Long recordedBy;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String recordingUrl;
    private Boolean isActive;
}

