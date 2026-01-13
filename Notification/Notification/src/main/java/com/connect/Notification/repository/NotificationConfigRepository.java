package com.connect.Notification.repository;

import com.connect.Notification.entity.Notification;
import com.connect.Notification.entity.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {
    Optional<NotificationConfig> findByOrganizationIdAndNotificationType(Long organizationId, Notification.NotificationType type);
    List<NotificationConfig> findByOrganizationId(Long organizationId);
}

