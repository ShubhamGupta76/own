package com.connect.Team.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Team entity
 * Teams belong to an organization
 */
@Document(collection = "teams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed
    private String organizationId;
    
    private String createdBy; // User ID who created the team (ADMIN or MANAGER)
    
    private String description;
    
    @Builder.Default
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

