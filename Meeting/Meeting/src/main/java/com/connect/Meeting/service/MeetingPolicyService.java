package com.connect.Meeting.service;

import com.connect.Meeting.entity.MeetingPolicy;
import com.connect.Meeting.repository.MeetingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingPolicyService {
    
    private final MeetingPolicyRepository meetingPolicyRepository;
    
    public MeetingPolicy getMeetingPolicy(String organizationId) {
        return meetingPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> MeetingPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .maxDurationMinutes(60)
                        .maxParticipants(100)
                        .build());
    }
    
    public MeetingPolicy updateMeetingPolicy(String organizationId, Boolean enabled, Integer maxDurationMinutes, Integer maxParticipants) {
        MeetingPolicy policy = meetingPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> MeetingPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .maxDurationMinutes(60)
                        .maxParticipants(100)
                        .build());
        
        if (enabled != null) policy.setEnabled(enabled);
        if (maxDurationMinutes != null) policy.setMaxDurationMinutes(maxDurationMinutes);
        if (maxParticipants != null) policy.setMaxParticipants(maxParticipants);
        
        return meetingPolicyRepository.save(policy);
    }
}

