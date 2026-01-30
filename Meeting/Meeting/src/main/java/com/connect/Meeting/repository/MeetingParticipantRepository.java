package com.connect.Meeting.repository;

import com.connect.Meeting.entity.MeetingParticipant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MeetingParticipant entity
 */
@Repository
public interface MeetingParticipantRepository extends MongoRepository<MeetingParticipant, String> {
    
    List<MeetingParticipant> findByMeetingId(String meetingId);
    
    List<MeetingParticipant> findByUserId(String userId);
    
    Optional<MeetingParticipant> findByMeetingIdAndUserId(String meetingId, String userId);
    
    List<MeetingParticipant> findByMeetingIdAndIsActiveTrue(String meetingId);
    
    boolean existsByMeetingIdAndUserId(String meetingId, String userId);
}

