package com.connect.Meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for scheduling a meeting
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMeetingRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    private List<Long> participantIds; // User IDs to invite
    
    private Long teamId; // Optional: if meeting is for a team
    
    private Long channelId; // Optional: if meeting is for a channel
    
    private String meetingUrl; // Video conferencing URL
}

