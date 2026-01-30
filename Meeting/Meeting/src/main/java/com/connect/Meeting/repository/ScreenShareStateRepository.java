package com.connect.Meeting.repository;

import com.connect.Meeting.entity.ScreenShareState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ScreenShareState entity
 */
@Repository
public interface ScreenShareStateRepository extends MongoRepository<ScreenShareState, String> {
    
    Optional<ScreenShareState> findByMeetingId(String meetingId);
    
    Optional<ScreenShareState> findByMeetingIdAndIsActiveTrue(String meetingId);
}

