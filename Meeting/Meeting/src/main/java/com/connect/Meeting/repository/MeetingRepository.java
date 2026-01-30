package com.connect.Meeting.repository;

import com.connect.Meeting.entity.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Meeting entity
 */
@Repository
public interface MeetingRepository extends MongoRepository<Meeting, String> {
    
    List<Meeting> findByOrganizationId(String organizationId);
    
    List<Meeting> findByOrganizationIdAndStatus(String organizationId, Meeting.MeetingStatus status);
    
    List<Meeting> findByCreatedBy(String createdBy);
    
    Optional<Meeting> findByIdAndOrganizationId(String id, String organizationId);
}

