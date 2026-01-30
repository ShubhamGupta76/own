package com.connect.Notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "notification_configs")
@CompoundIndex(name = "org_type_idx", def = "{'organizationId': 1, 'notificationType': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationConfig {
    
    @Id
    private String id;
    
    @Indexed
    private String organizationId;
    
    private Notification.NotificationType notificationType;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

