package com.connect.Meeting.repository;

import com.connect.Meeting.entity.ScreenShareState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ScreenShareState entity
 */
@Repository
public interface ScreenShareStateRepository extends JpaRepository<ScreenShareState, Long> {
    
    Optional<ScreenShareState> findByMeetingId(Long meetingId);
    
    Optional<ScreenShareState> findByMeetingIdAndIsActiveTrue(Long meetingId);
}

