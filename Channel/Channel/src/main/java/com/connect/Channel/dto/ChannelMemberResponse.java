package com.connect.Channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberResponse {
    private String id;
    private String userId;
    private String channelId;
    private LocalDateTime joinedAt;
}

