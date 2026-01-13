package com.connect.Channel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Channel Member entity
 */
@Entity
@Table(name = "channel_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"channel_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", nullable = false)
    private Long channelId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}

