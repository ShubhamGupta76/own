package com.connect.Notification.repository;

import com.connect.Notification.entity.Notification;
import com.connect.Notification.entity.NotificationConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationConfigRepository extends MongoRepository<NotificationConfig, String> {
    Optional<NotificationConfig> findByOrganizationIdAndNotificationType(String organizationId, Notification.NotificationType type);
    List<NotificationConfig> findByOrganizationId(String organizationId);
}

