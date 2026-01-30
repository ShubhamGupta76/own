package com.connect.Chat.repository;

import com.connect.Chat.entity.ChatPolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatPolicyRepository extends MongoRepository<ChatPolicy, String> {
    Optional<ChatPolicy> findByOrganizationId(String organizationId);
}

