package com.connect.Chat.service;

import com.connect.Chat.event.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka Event Producer for Chat Service
 * Publishes chat events to Kafka topic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatEventProducer {
    
    private static final String CHAT_EVENTS_TOPIC = "chat-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish MESSAGE_SENT event
     */
    public void publishMessageSentEvent(Long messageId, Long channelId, Long chatRoomId, 
                                       Long senderId, String senderRole, 
                                       List<Long> mentionedUserIds, Long organizationId, 
                                       String messageContent) {
        // Skip Kafka publishing if template is not available (Kafka might be down)
        if (kafkaTemplate == null) {
            log.debug("KafkaTemplate not available, skipping event publishing");
            return;
        }
        
        try {
            ChatEvent event = ChatEvent.builder()
                    .eventType("MESSAGE_SENT")
                    .messageId(messageId)
                    .channelId(channelId)
                    .chatRoomId(chatRoomId)
                    .senderId(senderId)
                    .senderRole(senderRole)
                    .mentionedUserIds(mentionedUserIds)
                    .organizationId(organizationId)
                    .messageContent(messageContent)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Use organizationId as partition key for better distribution
            // Send asynchronously to avoid blocking if Kafka is unavailable
            // Use exceptionally() to handle errors immediately without blocking
            kafkaTemplate.send(CHAT_EVENTS_TOPIC, organizationId.toString(), event)
                    .exceptionally(ex -> {
                        log.warn("Failed to publish MESSAGE_SENT event for messageId: {}, organizationId: {} - {}", 
                                messageId, organizationId, ex.getMessage());
                        return null; // Return null to indicate failure
                    })
                    .thenAccept(result -> {
                        if (result != null) {
                            log.debug("Published MESSAGE_SENT event for messageId: {}, organizationId: {}", messageId, organizationId);
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to publish MESSAGE_SENT event: {} - continuing without publishing", e.getMessage());
            // Don't throw exception - event publishing failure shouldn't break the main flow
        }
    }
}

