package com.fixflow.model;

/**
 * Lifecycle states of a maintenance ticket.
 * Standard flow: OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED.
 * Cancellation: OPEN/ASSIGNED -> CANCELLED.
 */
public enum TicketStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
