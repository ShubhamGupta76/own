package com.connect.Meeting.repository;

import com.connect.Meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Meeting entity
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
    List<Meeting> findByOrganizationId(Long organizationId);
    
    List<Meeting> findByOrganizationIdAndStatus(Long organizationId, Meeting.MeetingStatus status);
    
    List<Meeting> findByCreatedBy(Long createdBy);
    
    Optional<Meeting> findByIdAndOrganizationId(Long id, Long organizationId);
}

