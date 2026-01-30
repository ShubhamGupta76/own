package com.connect.Chat.service;

import com.connect.Chat.entity.ChatPolicy;
import com.connect.Chat.repository.ChatPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ChatPolicyService {
    
    private final ChatPolicyRepository chatPolicyRepository;
    
    public ChatPolicy getChatPolicy(String organizationId) {
        return chatPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> ChatPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .build());
    }
    
    public ChatPolicy updateChatPolicy(String organizationId, Boolean enabled) {
        ChatPolicy policy = chatPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> ChatPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(enabled)
                        .build());
        
        policy.setEnabled(enabled);
        return chatPolicyRepository.save(policy);
    }
}

