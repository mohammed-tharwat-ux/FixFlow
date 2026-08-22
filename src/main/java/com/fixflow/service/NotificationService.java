package com.fixflow.service;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Notification;
import com.fixflow.model.NotificationType;
import com.fixflow.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Service managing in-system notifications for events such as ticket creation,
 * technician assignment, status transitions, and SLA warnings.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = Objects.requireNonNull(notificationRepository, "notificationRepository cannot be null");
    }

    /**
     * Sends an in-system notification.
     */
    public Notification sendNotification(Long recipientId, Long ticketId, NotificationType type, String title, String message) {
        if (recipientId == null || recipientId <= 0) {
            throw new ValidationException("Recipient ID must be a positive number");
        }
        if (type == null) {
            throw new ValidationException("Notification type cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Notification title cannot be empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("Notification message cannot be empty");
        }

        Notification notification = new Notification(
                null,
                recipientId,
                title.trim(),
                message.trim(),
                ticketId,
                type,
                LocalDateTime.now(),
                false
        );

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long recipientId) {
        if (recipientId == null) {
            throw new ValidationException("Recipient ID cannot be null");
        }
        return notificationRepository.findByRecipientId(recipientId);
    }

    public List<Notification> getUnreadNotifications(Long recipientId) {
        if (recipientId == null) {
            throw new ValidationException("Recipient ID cannot be null");
        }
        return notificationRepository.findUnreadByRecipientId(recipientId);
    }

    public void markAsRead(Long notificationId) {
        if (notificationId == null) {
            throw new ValidationException("Notification ID cannot be null");
        }
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ValidationException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.update(notification);
    }
}
