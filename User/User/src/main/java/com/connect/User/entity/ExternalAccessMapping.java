package com.connect.User.entity;

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
 * External Access Mapping entity
 * Maps external users to specific teams/channels they can access
 * Restricts external users to only their assigned teams/channels
 */
@Document(collection = "external_access_mappings")
@CompoundIndex(name = "user_team_channel_idx", def = "{'userId': 1, 'teamId': 1, 'channelId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccessMapping {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // External user ID (String from User entity)
    
    @Indexed
    private String organizationId;
    
    private String teamId; // Optional: if access is for entire team
    
    private String channelId; // Optional: if access is for specific channel
    
    private String grantedBy; // ADMIN who granted access (String from User entity)
    
    @CreatedDate
    private LocalDateTime grantedAt;
    
    private LocalDateTime expiresAt; // Optional: access expiration
}

