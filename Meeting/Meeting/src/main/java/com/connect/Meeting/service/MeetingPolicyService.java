package com.connect.Meeting.service;

import com.connect.Meeting.entity.MeetingPolicy;
import com.connect.Meeting.repository.MeetingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingPolicyService {
    
    private final MeetingPolicyRepository meetingPolicyRepository;
    
    @Transactional(readOnly = true)
    public MeetingPolicy getMeetingPolicy(Long organizationId) {
        return meetingPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> MeetingPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .maxDurationMinutes(60)
                        .maxParticipants(100)
                        .build());
    }
    
    @Transactional
    public MeetingPolicy updateMeetingPolicy(Long organizationId, Boolean enabled, Integer maxDurationMinutes, Integer maxParticipants) {
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

