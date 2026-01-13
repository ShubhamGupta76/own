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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ExternalAccessResponse inviteExternalUser(InviteExternalUserRequest request, 
                                                     Long grantedBy, Long organizationId) {
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
            for (Long teamId : request.getTeamIds()) {
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
            for (Long channelId : request.getChannelIds()) {
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
    @Transactional(readOnly = true)
    public ExternalAccessResponse getExternalAccess(Long userId, Long organizationId) {
        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != User.Role.EXTERNAL_USER) {
            throw new RuntimeException("User is not an external user");
        }
        
        List<ExternalAccessMapping> mappings = accessMappingRepository
                .findByUserIdAndOrganizationId(userId, organizationId);
        
        List<Long> teamIds = mappings.stream()
                .filter(m -> m.getTeamId() != null)
                .map(ExternalAccessMapping::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> channelIds = mappings.stream()
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
    @Transactional(readOnly = true)
    public boolean hasTeamAccess(Long userId, Long teamId) {
        return accessMappingRepository.existsByUserIdAndTeamId(userId, teamId);
    }
    
    /**
     * Check if external user has access to a channel
     */
    @Transactional(readOnly = true)
    public boolean hasChannelAccess(Long userId, Long channelId) {
        return accessMappingRepository.existsByUserIdAndChannelId(userId, channelId);
    }
    
    /**
     * Map User to ExternalAccessResponse
     */
    private ExternalAccessResponse mapToResponse(User user) {
        List<ExternalAccessMapping> mappings = accessMappingRepository
                .findByUserIdAndOrganizationId(user.getId(), user.getOrganizationId());
        
        List<Long> teamIds = mappings.stream()
                .filter(m -> m.getTeamId() != null)
                .map(ExternalAccessMapping::getTeamId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> channelIds = mappings.stream()
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

