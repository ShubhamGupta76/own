package com.connect.Channel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Channel Permission entity
 * Defines permissions for channels
 */
@Entity
@Table(name = "channel_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", nullable = false)
    private Long channelId;
    
    @Column(name = "user_id")
    private Long userId; // null for team-level permissions
    
    @Column(name = "team_id")
    private Long teamId; // null for user-level permissions
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionType permissionType;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    public enum PermissionType {
        READ,
        WRITE,
        DELETE,
        MANAGE
    }
}

