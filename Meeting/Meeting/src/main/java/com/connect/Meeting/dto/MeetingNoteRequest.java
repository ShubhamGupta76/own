package com.connect.Meeting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a meeting note
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingNoteRequest {
    
    @NotBlank(message = "Note content is required")
    private String content;
}

