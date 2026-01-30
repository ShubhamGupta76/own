package com.connect.Meeting.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Meeting Event DTO for Kafka
 * Published for various meeting events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingEvent {
    
    private String eventType; // MEETING_CREATED, USER_JOINED, USER_LEFT, RECORDING_STARTED, RECORDING_STOPPED
    private String meetingId;
    private String meetingTitle;
    private String userId; // For USER_JOINED, USER_LEFT, RECORDING_STARTED, RECORDING_STOPPED
    private String organizationId;
    private String teamId;
    private String channelId;
    private List<String> participantIds; // For MEETING_CREATED
    private String recordingUrl; // For RECORDING_STOPPED
    private LocalDateTime timestamp;
}

