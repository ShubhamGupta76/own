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
            kafkaTemplate.send(CHAT_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published MESSAGE_SENT event for messageId: {}, organizationId: {}", messageId, organizationId);
        } catch (Exception e) {
            log.error("Failed to publish MESSAGE_SENT event: {}", e.getMessage(), e);
            // Don't throw exception - event publishing failure shouldn't break the main flow
        }
    }
}

