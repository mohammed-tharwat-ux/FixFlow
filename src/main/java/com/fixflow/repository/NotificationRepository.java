package com.fixflow.repository;

import com.fixflow.model.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for Notifications.
 */
public interface NotificationRepository {

    Optional<Notification> findById(Long id);

    Notification save(Notification notification);

    Notification update(Notification notification);

    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findUnreadByRecipientId(Long recipientId);

    List<Notification> findAll();

    void clear();
}
