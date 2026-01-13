package com.connect.Channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelResponse {
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private Long organizationId;
    private String type;
    private Boolean active;
    private Boolean chatEnabled;
    private Boolean fileEnabled;
    private Boolean meetingEnabled;
    private LocalDateTime createdAt;
    private List<ChannelMemberResponse> members;
    private List<ChannelPermissionResponse> permissions;
}

