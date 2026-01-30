package com.connect.Team.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Team Member entity
 * Represents membership of a user in a team
 */
@Document(collection = "team_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {
    
    @Id
    private String id;
    
    @Indexed
    private String teamId;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String organizationId;
    
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;
    
    @CreatedDate
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        OWNER,
        ADMIN,
        MEMBER
    }
}

