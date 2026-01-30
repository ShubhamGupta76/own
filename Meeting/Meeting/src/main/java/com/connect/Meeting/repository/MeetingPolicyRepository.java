package com.connect.Meeting.repository;

import com.connect.Meeting.entity.MeetingPolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingPolicyRepository extends MongoRepository<MeetingPolicy, String> {
    Optional<MeetingPolicy> findByOrganizationId(String organizationId);
}

