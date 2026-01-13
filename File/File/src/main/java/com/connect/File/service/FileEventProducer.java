package com.connect.File.service;

import com.connect.File.event.FileEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka Event Producer for File Service
 * Publishes file events to Kafka topic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileEventProducer {
    
    private static final String FILE_EVENTS_TOPIC = "file-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish FILE_UPLOADED event
     */
    public void publishFileUploadedEvent(Long fileId, String filename, Long channelId,
                                       Long uploadedBy, Long organizationId, 
                                       Long fileSize, String contentType) {
        try {
            FileEvent event = FileEvent.builder()
                    .eventType("FILE_UPLOADED")
                    .fileId(fileId)
                    .filename(filename)
                    .channelId(channelId)
                    .uploadedBy(uploadedBy)
                    .organizationId(organizationId)
                    .fileSize(fileSize)
                    .contentType(contentType)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(FILE_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published FILE_UPLOADED event for fileId: {}, organizationId: {}", fileId, organizationId);
        } catch (Exception e) {
            log.error("Failed to publish FILE_UPLOADED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish FILE_DELETED event
     */
    public void publishFileDeletedEvent(Long fileId, String filename, Long channelId,
                                       Long uploadedBy, Long organizationId) {
        try {
            FileEvent event = FileEvent.builder()
                    .eventType("FILE_DELETED")
                    .fileId(fileId)
                    .filename(filename)
                    .channelId(channelId)
                    .uploadedBy(uploadedBy)
                    .organizationId(organizationId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(FILE_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published FILE_DELETED event for fileId: {}, organizationId: {}", fileId, organizationId);
        } catch (Exception e) {
            log.error("Failed to publish FILE_DELETED event: {}", e.getMessage(), e);
        }
    }
}

