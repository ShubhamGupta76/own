package com.connect.Channel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Channel Permission entity
 * Defines permissions for channels
 */
@Document(collection = "channel_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPermission {
    
    @Id
    private String id;
    
    @Indexed
    private String channelId;
    
    private String userId; // null for team-level permissions
    
    private String teamId; // null for user-level permissions
    
    private PermissionType permissionType;
    
    @Indexed
    private String organizationId;
    
    public enum PermissionType {
        READ,
        WRITE,
        DELETE,
        MANAGE
    }
}

