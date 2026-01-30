package com.connect.User.service;

import com.connect.User.dto.ExternalAccessResponse;
import com.connect.User.dto.InviteExternalUserRequest;
import com.connect.User.entity.ExternalAccessMapping;
import com.connect.User.entity.User;
import com.connect.User.repository.ExternalAccessMappingRepository;
import com.connect.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for external user access management
 * Handles inviting external users and managing their access to teams/channels
 */
@Service
@RequiredArgsConstructor
public class ExternalAccessService {
    
    private final UserRepository userRepository;
    private final ExternalAccessMappingRepository accessMappingRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Invite an external user
     * Only ADMIN can invite external users
     */
    public ExternalAccessResponse inviteExternalUser(InviteExternalUserRequest request, 
                                                     String grantedBy, String organizationId) {
        // Check if user already exists
        if (userRepository.findByEmailAndOrganizationId(request.getEmail(), organizationId).isPresent()) {
            throw new RuntimeException("User with this email already exists in the organization");
        }
        
        // Generate temporary password for external user
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        
        // Create external user
        User externalUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .organizationId(organizationId)
                .role(User.Role.EXTERNAL_USER)
                .active(true)
                .isFirstLogin(true)
                .build();
        
        externalUser = userRepository.save(externalUser);
        
        // Grant access to teams
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            for (String teamId : request.getTeamIds()) {
                ExternalAccessMapping mapping = ExternalAccessMapping.builder()
                        .userId(externalUser.getId())
                        .organizationId(organizationId)
                        .teamId(teamId)
                        .channelId(null)
                        .grantedBy(grantedBy)
                        .build();
                accessMappingRepository.save(mapping);
            }
        }
        
        // Grant access to channels
        if (request.getChannelIds() != null && !request.getChannelIds().isEmpty()) {
            for (String channelId : request.getChannelIds()) {
                ExternalAccessMapping mapping = ExternalAccessMapping.builder()
                        .userId(externalUser.getId())
                        .organizationId(organizationId)
                        .channelId(channelId)
                        .teamId(null)
                        .grantedBy(grantedBy)
                        .build();
                accessMappingRepository.save(mapping);
            }
        }
        
        // Return response (password should be sent separately via email)
        return mapToResponse(externalUser);
    }
    
    /**
     * Get external access for a user
     */
    public ExternalAccessResponse getExternalAccess(String userId, String organizationId) {
        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.EXTERNAL_USER) {
            throw new RuntimeException("User is not an external user");
        }
        
        List<ExternalAccessMapping> mappings = accessMappingRepository
                .findByUserIdAndOrganizationId(userId, organizationId);
        
        List<String> teamIds = mappings.stream()
                .filter(m -> m.getTeamId() != null)
                .map(ExternalAccessMapping::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        List<String> channelIds = mappings.stream()
                .filter(m -> m.getChannelId() != null)
                .map(ExternalAccessMapping::getChannelId)
                .distinct()
                .collect(Collectors.toList());
        
        return ExternalAccessResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(organizationId)
                .teamIds(teamIds)
                .channelIds(channelIds)
                .grantedAt(mappings.isEmpty() ? null : mappings.get(0).getGrantedAt())
                .expiresAt(mappings.isEmpty() ? null : mappings.get(0).getExpiresAt())
                .build();
    }
    
    /**
     * Check if external user has access to a team
     */
    public boolean hasTeamAccess(String userId, String teamId) {
        return accessMappingRepository.existsByUserIdAndTeamId(userId, teamId);
    }
    
    /**
     * Check if external user has access to a channel
     */
    public boolean hasChannelAccess(String userId, String channelId) {
        return accessMappingRepository.existsByUserIdAndChannelId(userId, channelId);
    }
    
    /**
     * Map User to ExternalAccessResponse
     */
    private ExternalAccessResponse mapToResponse(User user) {
        List<ExternalAccessMapping> mappings = accessMappingRepository
                .findByUserIdAndOrganizationId(user.getId(), user.getOrganizationId());
        
        List<String> teamIds = mappings.stream()
                .filter(m -> m.getTeamId() != null)
                .map(ExternalAccessMapping::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        List<String> channelIds = mappings.stream()
                .filter(m -> m.getChannelId() != null)
                .map(ExternalAccessMapping::getChannelId)
                .distinct()
                .collect(Collectors.toList());
        
        return ExternalAccessResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganizationId())
                .teamIds(teamIds)
                .channelIds(channelIds)
                .grantedAt(mappings.isEmpty() ? null : mappings.get(0).getGrantedAt())
                .expiresAt(mappings.isEmpty() ? null : mappings.get(0).getExpiresAt())
                .build();
    }
}

