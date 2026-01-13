package com.connect.Meeting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Meeting Note entity
 * Stores notes taken during meetings
 */
@Entity
@Table(name = "meeting_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // User ID who created the note
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Note content
    
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

