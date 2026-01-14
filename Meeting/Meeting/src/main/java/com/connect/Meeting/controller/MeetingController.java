package com.connect.Meeting.controller;

import com.connect.Meeting.dto.*;
import com.connect.Meeting.service.MeetingService;
import com.connect.Meeting.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for meeting operations
 * Handles instant calls, scheduled meetings, participants, notes, screen sharing, and recording
 */
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Management", description = "Meeting APIs for instant calls, scheduled meetings, and collaboration features")
@SecurityRequirement(name = "bearerAuth")
public class MeetingController {
    
    private final MeetingService meetingService;
    private final JwtUtil jwtUtil;
    
    /**
     * Extract user information from JWT token
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUserId(token);
    }
    
    private Long getOrganizationId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractOrganizationId(token);
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
    
    /**
     * Create an instant call
     * POST /api/meetings/instant
     */
    @PostMapping("/instant")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Create instant call", description = "Creates an instant call that starts immediately. No scheduling required.")
    public ResponseEntity<MeetingResponse> createInstantCall(
            @Valid @RequestBody InstantCallRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MeetingResponse meeting = meetingService.createInstantCall(request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Schedule a meeting
     * POST /api/meetings/schedule
     */
    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Schedule meeting", description = "Schedules a meeting for a future time with participants.")
    public ResponseEntity<MeetingResponse> scheduleMeeting(
            @Valid @RequestBody ScheduleMeetingRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MeetingResponse meeting = meetingService.scheduleMeeting(request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Join a meeting
     * POST /api/meetings/{id}/join
     */
    @PostMapping("/{id}/join")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Join meeting", description = "Joins a meeting. If meeting is scheduled and time has come, it becomes live.")
    public ResponseEntity<MeetingParticipantResponse> joinMeeting(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MeetingParticipantResponse participant = meetingService.joinMeeting(id, userId, organizationId);
            return ResponseEntity.ok(participant);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Leave a meeting
     * POST /api/meetings/{id}/leave
     */
    @PostMapping("/{id}/leave")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Leave meeting", description = "Leaves a meeting. If no active participants remain, meeting ends.")
    public ResponseEntity<Void> leaveMeeting(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            meetingService.leaveMeeting(id, userId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Start screen sharing
     * POST /api/meetings/{id}/screen-share/start
     */
    @PostMapping("/{id}/screen-share/start")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Start screen sharing", description = "Starts screen sharing in a live meeting.")
    public ResponseEntity<ScreenShareStateResponse> startScreenShare(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            ScreenShareStateResponse screenShare = meetingService.startScreenShare(id, userId, organizationId);
            return ResponseEntity.ok(screenShare);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Stop screen sharing
     * POST /api/meetings/{id}/screen-share/stop
     */
    @PostMapping("/{id}/screen-share/stop")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Stop screen sharing", description = "Stops screen sharing in a meeting.")
    public ResponseEntity<Void> stopScreenShare(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            meetingService.stopScreenShare(id, userId, organizationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Start recording
     * POST /api/meetings/{id}/recording/start
     */
    @PostMapping("/{id}/recording/start")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Start recording", description = "Starts recording a live meeting.")
    public ResponseEntity<RecordingStateResponse> startRecording(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            RecordingStateResponse recording = meetingService.startRecording(id, userId, organizationId);
            return ResponseEntity.ok(recording);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Stop recording
     * POST /api/meetings/{id}/recording/stop
     */
    @PostMapping("/{id}/recording/stop")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Stop recording", description = "Stops recording and saves recording URL.")
    public ResponseEntity<RecordingStateResponse> stopRecording(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            String recordingUrl = request != null ? request.get("recordingUrl") : null;
            RecordingStateResponse recording = meetingService.stopRecording(id, userId, organizationId, recordingUrl);
            return ResponseEntity.ok(recording);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Add a note to a meeting
     * POST /api/meetings/{id}/notes
     */
    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Add meeting note", description = "Adds a text note to a meeting.")
    public ResponseEntity<MeetingNoteResponse> addNote(
            @PathVariable Long id,
            @Valid @RequestBody MeetingNoteRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserId(httpRequest);
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MeetingNoteResponse note = meetingService.addNote(id, request, userId, organizationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(note);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get notes for a meeting
     * GET /api/meetings/{id}/notes
     */
    @GetMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get meeting notes", description = "Retrieves all notes for a meeting.")
    public ResponseEntity<List<MeetingNoteResponse>> getNotes(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<MeetingNoteResponse> notes = meetingService.getNotes(id, organizationId);
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get meeting by ID
     * GET /api/meetings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get meeting", description = "Retrieves meeting details with participants, screen share, and recording status.")
    public ResponseEntity<MeetingResponse> getMeeting(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            MeetingResponse meeting = meetingService.getMeeting(id, organizationId);
            return ResponseEntity.ok(meeting);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Get all meetings
     * GET /api/meetings
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    @Operation(summary = "Get all meetings", description = "Retrieves all meetings in the organization.")
    public ResponseEntity<List<MeetingResponse>> getAllMeetings(HttpServletRequest httpRequest) {
        try {
            Long organizationId = getOrganizationId(httpRequest);
            
            if (organizationId == null) {
                throw new RuntimeException("Organization not found");
            }
            
            List<MeetingResponse> meetings = meetingService.getAllMeetings(organizationId);
            return ResponseEntity.ok(meetings);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

