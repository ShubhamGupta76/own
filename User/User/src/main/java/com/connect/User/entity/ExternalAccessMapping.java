package com.connect.User.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * External Access Mapping entity
 * Maps external users to specific teams/channels they can access
 * Restricts external users to only their assigned teams/channels
 */
@Entity
@Table(name = "external_access_mappings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "team_id", "channel_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccessMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // External user ID
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "team_id")
    private Long teamId; // Optional: if access is for entire team
    
    @Column(name = "channel_id")
    private Long channelId; // Optional: if access is for specific channel
    
    @Column(name = "granted_by", nullable = false)
    private Long grantedBy; // ADMIN who granted access
    
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional: access expiration
    
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}

