package com.connect.Auth.entity;

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

@Document(collection = "admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    @Indexed
    private String organizationId; 
    
    private Role role = Role.ADMIN; 
    
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN
    }
}

