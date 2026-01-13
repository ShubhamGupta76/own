package com.connect.Notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private Long organizationId;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}

