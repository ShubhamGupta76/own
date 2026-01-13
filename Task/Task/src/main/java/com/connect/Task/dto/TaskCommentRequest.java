package com.connect.Task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a task comment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentRequest {
    
    @NotBlank(message = "Comment content is required")
    private String content;
}

