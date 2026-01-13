package com.connect.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for external access response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccessResponse {
    
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Long organizationId;
    private List<Long> teamIds;
    private List<Long> channelIds;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
}

