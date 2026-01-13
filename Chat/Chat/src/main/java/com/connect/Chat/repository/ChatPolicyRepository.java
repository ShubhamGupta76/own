package com.connect.Chat.repository;

import com.connect.Chat.entity.ChatPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatPolicyRepository extends JpaRepository<ChatPolicy, Long> {
    Optional<ChatPolicy> findByOrganizationId(Long organizationId);
}

