package com.connect.Meeting.repository;

import com.connect.Meeting.entity.RecordingState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RecordingState entity
 */
@Repository
public interface RecordingStateRepository extends JpaRepository<RecordingState, Long> {
    
    Optional<RecordingState> findByMeetingId(Long meetingId);
    
    Optional<RecordingState> findByMeetingIdAndIsActiveTrue(Long meetingId);
}

