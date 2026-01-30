package com.connect.Notification.repository;

import com.connect.Notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByOrganizationId(String organizationId);
    List<Notification> findByOrganizationIdAndEnabledTrue(String organizationId);
    
    List<Notification> findByUserIdAndOrganizationIdOrderByCreatedAtDesc(String userId, String organizationId);
    
    List<Notification> findByUserIdAndReadFalseAndOrganizationIdOrderByCreatedAtDesc(String userId, String organizationId);
    
    long countByUserIdAndReadFalseAndOrganizationId(String userId, String organizationId);
}

