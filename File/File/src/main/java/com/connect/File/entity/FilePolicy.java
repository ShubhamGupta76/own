package com.connect.File.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_policies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilePolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "organization_id", nullable = false, unique = true)
    private Long organizationId;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "max_file_size_mb")
    private Integer maxFileSizeMb;
    
    @Column(name = "allowed_file_types")
    private String allowedFileTypes; // Comma-separated
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

