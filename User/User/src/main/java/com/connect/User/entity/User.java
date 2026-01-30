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
 * User entity
 * Users belong to an organization and have a role (ADMIN, MANAGER, EMPLOYEE)
 */
@Document(collection = "users")
@CompoundIndex(name = "email_org_idx", def = "{'email': 1, 'organizationId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private String id;
    
    @Indexed
    private String email;
    
    private String password; // Encrypted password for employee login
    
    private String firstName;
    
    private String lastName;
    
    private String displayName; // Auto-generated on first login
    
    private LocalDateTime lastLoginAt; // Track last login time
    
    private Boolean isFirstLogin = true; // Track if this is first login
    
    @Indexed
    private String organizationId;
    
    private Role role;
    
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN,
        MANAGER,
        EMPLOYEE,
        EXTERNAL_USER  // External users (vendors/clients) with limited access
    }
}

