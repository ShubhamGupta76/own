package com.connect.Channel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Channel entity
 * Channels belong to teams
 */
@Document(collection = "channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed
    private String teamId;
    
    @Indexed
    private String organizationId;
    
    private String description;
    
    @Builder.Default
    private ChannelType type = ChannelType.STANDARD;
    
    private String createdBy; // User ID who created the channel
    
    @Builder.Default
    private Boolean chatEnabled = true;
    
    @Builder.Default
    private Boolean fileEnabled = true;
    
    @Builder.Default
    private Boolean meetingEnabled = true;
    
    @Builder.Default
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public enum ChannelType {
        STANDARD,  // Standard channel (default)
        PRIVATE    // Private channel
    }
}

