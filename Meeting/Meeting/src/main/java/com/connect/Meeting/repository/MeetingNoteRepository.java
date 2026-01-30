package com.connect.Meeting.repository;

import com.connect.Meeting.entity.MeetingNote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MeetingNote entity
 */
@Repository
public interface MeetingNoteRepository extends MongoRepository<MeetingNote, String> {
    
    List<MeetingNote> findByMeetingIdOrderByCreatedAtDesc(String meetingId);
    
    List<MeetingNote> findByMeetingIdAndOrganizationId(String meetingId, String organizationId);
}

