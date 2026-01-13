package com.connect.Meeting.repository;

import com.connect.Meeting.entity.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MeetingNote entity
 */
@Repository
public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
    
    List<MeetingNote> findByMeetingIdOrderByCreatedAtDesc(Long meetingId);
    
    List<MeetingNote> findByMeetingIdAndOrganizationId(Long meetingId, Long organizationId);
}

