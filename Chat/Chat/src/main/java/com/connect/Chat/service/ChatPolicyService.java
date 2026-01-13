package com.connect.Chat.service;

import com.connect.Chat.entity.ChatPolicy;
import com.connect.Chat.repository.ChatPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatPolicyService {
    
    private final ChatPolicyRepository chatPolicyRepository;
    
    @Transactional(readOnly = true)
    public ChatPolicy getChatPolicy(Long organizationId) {
        return chatPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> ChatPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .build());
    }
    
    @Transactional
    public ChatPolicy updateChatPolicy(Long organizationId, Boolean enabled) {
        ChatPolicy policy = chatPolicyRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> ChatPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(enabled)
                        .build());
        
        policy.setEnabled(enabled);
        return chatPolicyRepository.save(policy);
    }
}

