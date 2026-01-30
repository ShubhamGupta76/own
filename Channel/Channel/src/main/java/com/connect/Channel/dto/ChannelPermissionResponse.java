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
    private String id;
    private String channelId;
    private String userId;
    private String teamId;
    private String permissionType;
}

