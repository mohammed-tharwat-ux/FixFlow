package com.fixflow.model;

/**
 * Event types triggering in-system notifications.
 */
public enum NotificationType {
    TICKET_CREATED,
    TECHNICIAN_ASSIGNED,
    STATUS_CHANGED,
    TICKET_RESOLVED,
    TICKET_CLOSED,
    SLA_WARNING,
    SLA_VIOLATED
}
