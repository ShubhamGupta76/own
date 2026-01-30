package com.connect.User.entity;

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

/**
 * Policy entity for organization-level feature control
 * Controls: Chat, Meeting, File Sharing
 */
@Document(collection = "policies")
@CompoundIndex(name = "org_policy_idx", def = "{'organizationId': 1, 'policyType': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    
    @Id
    private String id;
    
    @Indexed
    private String organizationId;
    
    private PolicyType policyType;
    
    private Boolean enabled = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum PolicyType {
        CHAT,
        MEETING,
        FILE_SHARING
    }
}

