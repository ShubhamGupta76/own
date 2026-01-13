package com.connect.Meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for meeting response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    
    private Long id;
    private String title;
    private String description;
    private Long organizationId;
    private Long createdBy;
    private String meetingType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Long teamId;
    private Long channelId;
    private String meetingUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MeetingParticipantResponse> participants;
    private ScreenShareStateResponse screenShare;
    private RecordingStateResponse recording;
}

