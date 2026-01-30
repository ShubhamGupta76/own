package com.connect.Meeting.repository;

import com.connect.Meeting.entity.RecordingState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RecordingState entity
 */
@Repository
public interface RecordingStateRepository extends MongoRepository<RecordingState, String> {
    
    Optional<RecordingState> findByMeetingId(String meetingId);
    
    Optional<RecordingState> findByMeetingIdAndIsActiveTrue(String meetingId);
}

