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

import java.time.LocalDateTime;

/**
 * Organization entity (tenant/company)
 * Each organization is isolated with its own users and policies
 */
@Document(collection = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String name;
    
    @Indexed(unique = true, sparse = true)
    private String domain; // Company domain for email validation
    
    @Indexed
    private String adminId; // Reference to admin who created this organization (String from Auth service)
    
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

