package com.connect.Meeting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating an instant call
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstantCallRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private List<Long> participantIds; // User IDs to invite (optional for 1-to-1)
    
    private Long teamId; // Optional: if call is for a team
    
    private Long channelId; // Optional: if call is for a channel
    
    private String meetingUrl; // Video conferencing URL
}

