package com.connect.Channel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

/**
 * Channel Member entity
 */
@Document(collection = "channel_members")
@CompoundIndex(name = "channel_user_idx", def = "{'channelId': 1, 'userId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMember {
    
    @Id
    private String id;
    
    @Indexed
    private String channelId;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String organizationId;
    
    @CreatedDate
    private LocalDateTime joinedAt;
}

