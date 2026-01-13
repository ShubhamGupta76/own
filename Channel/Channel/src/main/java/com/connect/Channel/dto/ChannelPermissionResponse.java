package com.connect.Channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPermissionResponse {
    private Long id;
    private Long channelId;
    private Long userId;
    private Long teamId;
    private String permissionType;
}

