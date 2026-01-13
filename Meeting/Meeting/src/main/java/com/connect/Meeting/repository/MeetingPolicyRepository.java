package com.connect.Meeting.repository;

import com.connect.Meeting.entity.MeetingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingPolicyRepository extends JpaRepository<MeetingPolicy, Long> {
    Optional<MeetingPolicy> findByOrganizationId(Long organizationId);
}

