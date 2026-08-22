package com.fixflow.service;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Notification;
import com.fixflow.model.NotificationType;
import com.fixflow.repository.InMemoryNotificationRepository;
import com.fixflow.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = new InMemoryNotificationRepository();
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    @DisplayName("Send and retrieve in-system notifications")
    void shouldSendAndRetrieveNotifications() {
        Notification sent = notificationService.sendNotification(
                10L,
                100L,
                NotificationType.TICKET_CREATED,
                "Ticket #100 Created",
                "Your incident has been recorded."
        );

        assertNotNull(sent.getId());
        assertEquals(10L, sent.getRecipientId());
        assertFalse(sent.isRead());

        List<Notification> unread = notificationService.getUnreadNotifications(10L);
        assertEquals(1, unread.size());

        notificationService.markAsRead(sent.getId());
        assertEquals(0, notificationService.getUnreadNotifications(10L).size());
        assertEquals(1, notificationService.getUserNotifications(10L).size());
    }

    @Test
    @DisplayName("Reject notification with invalid parameters")
    void shouldRejectInvalidNotificationInputs() {
        assertThrows(ValidationException.class, () ->
                notificationService.sendNotification(null, 100L, NotificationType.TICKET_CREATED, "Title", "Message"));

        assertThrows(ValidationException.class, () ->
                notificationService.sendNotification(10L, 100L, null, "Title", "Message"));

        assertThrows(ValidationException.class, () ->
                notificationService.sendNotification(10L, 100L, NotificationType.TICKET_CREATED, "", "Message"));
    }
}
