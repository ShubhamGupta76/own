package com.connect.Meeting.service;

import com.connect.Meeting.event.MeetingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka Event Producer for Meeting Service
 * Publishes meeting events to Kafka topic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingEventProducer {
    
    private static final String MEETING_EVENTS_TOPIC = "meeting-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Publish MEETING_CREATED event
     */
    public void publishMeetingCreatedEvent(Long meetingId, String meetingTitle, 
                                         Long organizationId, Long teamId, Long channelId,
                                         List<Long> participantIds) {
        try {
            MeetingEvent event = MeetingEvent.builder()
                    .eventType("MEETING_CREATED")
                    .meetingId(meetingId)
                    .meetingTitle(meetingTitle)
                    .organizationId(organizationId)
                    .teamId(teamId)
                    .channelId(channelId)
                    .participantIds(participantIds)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(MEETING_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published MEETING_CREATED event for meetingId: {}, organizationId: {}", meetingId, organizationId);
        } catch (Exception e) {
            log.error("Failed to publish MEETING_CREATED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish USER_JOINED event
     */
    public void publishUserJoinedEvent(Long meetingId, Long userId, Long organizationId) {
        try {
            MeetingEvent event = MeetingEvent.builder()
                    .eventType("USER_JOINED")
                    .meetingId(meetingId)
                    .userId(userId)
                    .organizationId(organizationId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(MEETING_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published USER_JOINED event for meetingId: {}, userId: {}", meetingId, userId);
        } catch (Exception e) {
            log.error("Failed to publish USER_JOINED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish USER_LEFT event
     */
    public void publishUserLeftEvent(Long meetingId, Long userId, Long organizationId) {
        try {
            MeetingEvent event = MeetingEvent.builder()
                    .eventType("USER_LEFT")
                    .meetingId(meetingId)
                    .userId(userId)
                    .organizationId(organizationId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(MEETING_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published USER_LEFT event for meetingId: {}, userId: {}", meetingId, userId);
        } catch (Exception e) {
            log.error("Failed to publish USER_LEFT event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish RECORDING_STARTED event
     */
    public void publishRecordingStartedEvent(Long meetingId, Long userId, Long organizationId) {
        try {
            MeetingEvent event = MeetingEvent.builder()
                    .eventType("RECORDING_STARTED")
                    .meetingId(meetingId)
                    .userId(userId)
                    .organizationId(organizationId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(MEETING_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published RECORDING_STARTED event for meetingId: {}, userId: {}", meetingId, userId);
        } catch (Exception e) {
            log.error("Failed to publish RECORDING_STARTED event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Publish RECORDING_STOPPED event
     */
    public void publishRecordingStoppedEvent(Long meetingId, Long userId, Long organizationId, String recordingUrl) {
        try {
            MeetingEvent event = MeetingEvent.builder()
                    .eventType("RECORDING_STOPPED")
                    .meetingId(meetingId)
                    .userId(userId)
                    .organizationId(organizationId)
                    .recordingUrl(recordingUrl)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(MEETING_EVENTS_TOPIC, organizationId.toString(), event);
            log.info("Published RECORDING_STOPPED event for meetingId: {}, userId: {}", meetingId, userId);
        } catch (Exception e) {
            log.error("Failed to publish RECORDING_STOPPED event: {}", e.getMessage(), e);
        }
    }
}

