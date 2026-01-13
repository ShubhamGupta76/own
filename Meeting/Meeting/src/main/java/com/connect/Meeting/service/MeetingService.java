package com.connect.Meeting.service;

import com.connect.Meeting.dto.*;
import com.connect.Meeting.entity.*;
import com.connect.Meeting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for meeting operations
 * Handles instant calls, scheduled meetings, participants, notes, screen sharing, and recording
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingNoteRepository noteRepository;
    private final ScreenShareStateRepository screenShareRepository;
    private final RecordingStateRepository recordingRepository;
    private final MeetingPolicyRepository policyRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MeetingEventProducer eventProducer;
    
    /**
     * Create an instant call
     * No scheduling required, starts immediately
     */
    @Transactional
    public MeetingResponse createInstantCall(InstantCallRequest request, Long createdBy, Long organizationId) {
        // Check meeting policy
        validateMeetingPolicy(organizationId);
        
        // Create meeting
        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .organizationId(organizationId)
                .createdBy(createdBy)
                .meetingType(Meeting.MeetingType.INSTANT)
                .status(Meeting.MeetingStatus.LIVE)
                .actualStartTime(LocalDateTime.now())
                .teamId(request.getTeamId())
                .channelId(request.getChannelId())
                .meetingUrl(request.getMeetingUrl())
                .build();
        
        meeting = meetingRepository.save(meeting);
        
        // Add creator as participant
        addParticipant(meeting.getId(), createdBy, organizationId);
        
        // Add other participants if provided
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            for (Long participantId : request.getParticipantIds()) {
                try {
                    addParticipant(meeting.getId(), participantId, organizationId);
                } catch (Exception e) {
                    log.warn("Failed to add participant {} to meeting {}: {}", participantId, meeting.getId(), e.getMessage());
                }
            }
        }
        
        // Broadcast meeting created event via WebSocket
        broadcastMeetingEvent(meeting.getId(), "MEETING_CREATED", meeting);
        
        // Publish Kafka event for async notification processing
        List<Long> participantIds = participantRepository.findByMeetingId(meeting.getId()).stream()
                .map(p -> p.getUserId())
                .collect(java.util.stream.Collectors.toList());
        eventProducer.publishMeetingCreatedEvent(
                meeting.getId(),
                meeting.getTitle(),
                organizationId,
                meeting.getTeamId(),
                meeting.getChannelId(),
                participantIds
        );
        
        return mapToMeetingResponse(meeting);
    }
    
    /**
     * Schedule a meeting
     */
    @Transactional
    public MeetingResponse scheduleMeeting(ScheduleMeetingRequest request, Long createdBy, Long organizationId) {
        // Check meeting policy
        validateMeetingPolicy(organizationId);
        
        // Validate time
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start time cannot be in the past");
        }
        
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }
        
        // Create meeting
        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .organizationId(organizationId)
                .createdBy(createdBy)
                .meetingType(Meeting.MeetingType.SCHEDULED)
                .status(Meeting.MeetingStatus.SCHEDULED)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .teamId(request.getTeamId())
                .channelId(request.getChannelId())
                .meetingUrl(request.getMeetingUrl())
                .build();
        
        meeting = meetingRepository.save(meeting);
        
        // Add creator as participant
        addParticipant(meeting.getId(), createdBy, organizationId);
        
        // Add other participants if provided
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            for (Long participantId : request.getParticipantIds()) {
                try {
                    addParticipant(meeting.getId(), participantId, organizationId);
                } catch (Exception e) {
                    log.warn("Failed to add participant {} to meeting {}: {}", participantId, meeting.getId(), e.getMessage());
                }
            }
        }
        
        // Broadcast meeting scheduled event
        broadcastMeetingEvent(meeting.getId(), "MEETING_SCHEDULED", meeting);
        
        return mapToMeetingResponse(meeting);
    }
    
    /**
     * Join a meeting
     */
    @Transactional
    public MeetingParticipantResponse joinMeeting(Long meetingId, Long userId, Long organizationId) {
        // Verify meeting exists and belongs to organization
        Meeting meeting = meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Check if meeting is live or scheduled
        if (meeting.getStatus() == Meeting.MeetingStatus.ENDED) {
            throw new RuntimeException("Cannot join an ended meeting");
        }
        
        // Start meeting if it's scheduled and time has come
        if (meeting.getStatus() == Meeting.MeetingStatus.SCHEDULED && 
            meeting.getStartTime() != null && 
            meeting.getStartTime().isBefore(LocalDateTime.now())) {
            meeting.setStatus(Meeting.MeetingStatus.LIVE);
            meeting.setActualStartTime(LocalDateTime.now());
            meetingRepository.save(meeting);
        }
        
        // Add or update participant
        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meetingId, userId)
                .orElseGet(() -> {
                    MeetingParticipant newParticipant = MeetingParticipant.builder()
                            .meetingId(meetingId)
                            .userId(userId)
                            .organizationId(organizationId)
                            .isActive(true)
                            .build();
                    return participantRepository.save(newParticipant);
                });
        
        // If participant exists but left, reactivate
        if (!participant.getIsActive()) {
            participant.setIsActive(true);
            participant.setJoinedAt(LocalDateTime.now());
            participant.setLeftAt(null);
            participant = participantRepository.save(participant);
        }
        
        // Broadcast user joined event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("event", "USER_JOINED");
        event.put("meetingId", meetingId);
        event.put("userId", userId);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishUserJoinedEvent(meetingId, userId, organizationId);
        
        return mapToParticipantResponse(participant);
    }
    
    /**
     * Leave a meeting
     */
    @Transactional
    public void leaveMeeting(Long meetingId, Long userId, Long organizationId) {
        // Verify meeting exists
        Meeting meeting = meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Find and update participant
        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a participant of this meeting"));
        
        participant.setIsActive(false);
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
        
        // Check if meeting should end (no active participants)
        List<MeetingParticipant> activeParticipants = participantRepository
                .findByMeetingIdAndIsActiveTrue(meetingId);
        
        if (activeParticipants.isEmpty() && meeting.getStatus() == Meeting.MeetingStatus.LIVE) {
            meeting.setStatus(Meeting.MeetingStatus.ENDED);
            meeting.setActualEndTime(LocalDateTime.now());
            meetingRepository.save(meeting);
        }
        
        // Broadcast user left event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("event", "USER_LEFT");
        event.put("meetingId", meetingId);
        event.put("userId", userId);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishUserLeftEvent(meetingId, userId, organizationId);
    }
    
    /**
     * Start screen sharing
     */
    @Transactional
    public ScreenShareStateResponse startScreenShare(Long meetingId, Long userId, Long organizationId) {
        // Verify meeting exists and is live
        Meeting meeting = meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        if (meeting.getStatus() != Meeting.MeetingStatus.LIVE) {
            throw new RuntimeException("Screen sharing can only be started in live meetings");
        }
        
        // Check if screen sharing is already active
        screenShareRepository.findByMeetingIdAndIsActiveTrue(meetingId)
                .ifPresent(existing -> {
                    throw new RuntimeException("Screen sharing is already active");
                });
        
        // Create or update screen share state
        ScreenShareState screenShare = ScreenShareState.builder()
                .meetingId(meetingId)
                .startedBy(userId)
                .isActive(true)
                .build();
        
        screenShare = screenShareRepository.save(screenShare);
        
        // Broadcast screen share started event
        Map<String, Object> event = new HashMap<>();
        event.put("event", "SCREEN_SHARE_STARTED");
        event.put("meetingId", meetingId);
        event.put("startedBy", userId);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
        
        return mapToScreenShareResponse(screenShare);
    }
    
    /**
     * Stop screen sharing
     */
    @Transactional
    public void stopScreenShare(Long meetingId, Long userId, Long organizationId) {
        // Verify meeting exists
        meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Find and stop screen sharing
        ScreenShareState screenShare = screenShareRepository
                .findByMeetingIdAndIsActiveTrue(meetingId)
                .orElseThrow(() -> new RuntimeException("Screen sharing is not active"));
        
        // Verify user has permission (only the one who started can stop, or admin)
        // For simplicity, we allow any participant to stop
        
        screenShare.setIsActive(false);
        screenShare.setEndedAt(LocalDateTime.now());
        screenShareRepository.save(screenShare);
        
        // Broadcast screen share stopped event
        Map<String, Object> event = new HashMap<>();
        event.put("event", "SCREEN_SHARE_STOPPED");
        event.put("meetingId", meetingId);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
    }
    
    /**
     * Start recording
     */
    @Transactional
    public RecordingStateResponse startRecording(Long meetingId, Long userId, Long organizationId) {
        // Verify meeting exists and is live
        Meeting meeting = meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        if (meeting.getStatus() != Meeting.MeetingStatus.LIVE) {
            throw new RuntimeException("Recording can only be started in live meetings");
        }
        
        // Check if recording is already active
        recordingRepository.findByMeetingIdAndIsActiveTrue(meetingId)
                .ifPresent(existing -> {
                    throw new RuntimeException("Recording is already active");
                });
        
        // Create recording state
        RecordingState recording = RecordingState.builder()
                .meetingId(meetingId)
                .recordedBy(userId)
                .isActive(true)
                .build();
        
        recording = recordingRepository.save(recording);
        
        // Broadcast recording started event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("event", "RECORDING_STARTED");
        event.put("meetingId", meetingId);
        event.put("recordedBy", userId);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishRecordingStartedEvent(meetingId, userId, organizationId);
        
        return mapToRecordingResponse(recording);
    }
    
    /**
     * Stop recording
     */
    @Transactional
    public RecordingStateResponse stopRecording(Long meetingId, Long userId, Long organizationId, String recordingUrl) {
        // Verify meeting exists
        meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Find and stop recording
        RecordingState recording = recordingRepository
                .findByMeetingIdAndIsActiveTrue(meetingId)
                .orElseThrow(() -> new RuntimeException("Recording is not active"));
        
        recording.setIsActive(false);
        recording.setEndedAt(LocalDateTime.now());
        if (recordingUrl != null) {
            recording.setRecordingUrl(recordingUrl);
        }
        recording = recordingRepository.save(recording);
        
        // Broadcast recording stopped event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("event", "RECORDING_STOPPED");
        event.put("meetingId", meetingId);
        event.put("recordingUrl", recordingUrl);
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
        
        // Publish Kafka event for async notification processing
        eventProducer.publishRecordingStoppedEvent(meetingId, userId, organizationId, recordingUrl);
        
        return mapToRecordingResponse(recording);
    }
    
    /**
     * Add a note to a meeting
     */
    @Transactional
    public MeetingNoteResponse addNote(Long meetingId, MeetingNoteRequest request, Long userId, Long organizationId) {
        // Verify meeting exists
        meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Create note
        MeetingNote note = MeetingNote.builder()
                .meetingId(meetingId)
                .createdBy(userId)
                .organizationId(organizationId)
                .content(request.getContent())
                .build();
        
        note = noteRepository.save(note);
        
        return mapToNoteResponse(note);
    }
    
    /**
     * Get notes for a meeting
     */
    @Transactional(readOnly = true)
    public List<MeetingNoteResponse> getNotes(Long meetingId, Long organizationId) {
        // Verify meeting exists
        meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        List<MeetingNote> notes = noteRepository.findByMeetingIdAndOrganizationId(meetingId, organizationId);
        
        return notes.stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get meeting by ID
     */
    @Transactional(readOnly = true)
    public MeetingResponse getMeeting(Long meetingId, Long organizationId) {
        Meeting meeting = meetingRepository.findByIdAndOrganizationId(meetingId, organizationId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        return mapToMeetingResponse(meeting);
    }
    
    /**
     * Get all meetings for organization
     */
    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetings(Long organizationId) {
        List<Meeting> meetings = meetingRepository.findByOrganizationId(organizationId);
        
        return meetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Helper: Add participant to meeting
     */
    private MeetingParticipant addParticipant(Long meetingId, Long userId, Long organizationId) {
        // Check if already a participant
        if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
            throw new RuntimeException("User is already a participant");
        }
        
        MeetingParticipant participant = MeetingParticipant.builder()
                .meetingId(meetingId)
                .userId(userId)
                .organizationId(organizationId)
                .isActive(false) // Will be active when they join
                .build();
        
        return participantRepository.save(participant);
    }
    
    /**
     * Helper: Validate meeting policy
     */
    private void validateMeetingPolicy(Long organizationId) {
        MeetingPolicy policy = policyRepository.findByOrganizationId(organizationId)
                .orElse(MeetingPolicy.builder()
                        .organizationId(organizationId)
                        .enabled(true)
                        .build());
        
        if (!policy.getEnabled()) {
            throw new RuntimeException("Meetings are disabled for your organization");
        }
    }
    
    /**
     * Helper: Broadcast meeting event via WebSocket
     */
    private void broadcastMeetingEvent(Long meetingId, String eventType, Meeting meeting) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", eventType);
        event.put("meeting", mapToMeetingResponse(meeting));
        messagingTemplate.convertAndSend("/topic/meeting/" + meetingId, (Object) event);
    }
    
    /**
     * Map Meeting entity to MeetingResponse DTO
     */
    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(meeting.getId());
        List<MeetingParticipantResponse> participantResponses = participants.stream()
                .map(this::mapToParticipantResponse)
                .collect(Collectors.toList());
        
        ScreenShareStateResponse screenShare = screenShareRepository.findByMeetingId(meeting.getId())
                .map(this::mapToScreenShareResponse)
                .orElse(null);
        
        RecordingStateResponse recording = recordingRepository.findByMeetingId(meeting.getId())
                .map(this::mapToRecordingResponse)
                .orElse(null);
        
        return MeetingResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .organizationId(meeting.getOrganizationId())
                .createdBy(meeting.getCreatedBy())
                .meetingType(meeting.getMeetingType().name())
                .status(meeting.getStatus().name())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .actualStartTime(meeting.getActualStartTime())
                .actualEndTime(meeting.getActualEndTime())
                .teamId(meeting.getTeamId())
                .channelId(meeting.getChannelId())
                .meetingUrl(meeting.getMeetingUrl())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .participants(participantResponses)
                .screenShare(screenShare)
                .recording(recording)
                .build();
    }
    
    /**
     * Map MeetingParticipant entity to DTO
     */
    private MeetingParticipantResponse mapToParticipantResponse(MeetingParticipant participant) {
        return MeetingParticipantResponse.builder()
                .id(participant.getId())
                .meetingId(participant.getMeetingId())
                .userId(participant.getUserId())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .isActive(participant.getIsActive())
                .build();
    }
    
    /**
     * Map MeetingNote entity to DTO
     */
    private MeetingNoteResponse mapToNoteResponse(MeetingNote note) {
        return MeetingNoteResponse.builder()
                .id(note.getId())
                .meetingId(note.getMeetingId())
                .createdBy(note.getCreatedBy())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
    
    /**
     * Map ScreenShareState entity to DTO
     */
    private ScreenShareStateResponse mapToScreenShareResponse(ScreenShareState screenShare) {
        return ScreenShareStateResponse.builder()
                .id(screenShare.getId())
                .meetingId(screenShare.getMeetingId())
                .startedBy(screenShare.getStartedBy())
                .startedAt(screenShare.getStartedAt())
                .endedAt(screenShare.getEndedAt())
                .isActive(screenShare.getIsActive())
                .build();
    }
    
    /**
     * Map RecordingState entity to DTO
     */
    private RecordingStateResponse mapToRecordingResponse(RecordingState recording) {
        return RecordingStateResponse.builder()
                .id(recording.getId())
                .meetingId(recording.getMeetingId())
                .recordedBy(recording.getRecordedBy())
                .startedAt(recording.getStartedAt())
                .endedAt(recording.getEndedAt())
                .recordingUrl(recording.getRecordingUrl())
                .isActive(recording.getIsActive())
                .build();
    }
}

