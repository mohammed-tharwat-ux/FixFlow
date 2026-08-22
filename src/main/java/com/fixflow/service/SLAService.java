package com.fixflow.service;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Service managing Service Level Agreement (SLA) calculations, deadline tracking,
 * and compliance verification.
 */
public class SLAService {

    // Target resolution windows as defined by system specifications
    public static final Duration SLA_CRITICAL = Duration.ofHours(2);
    public static final Duration SLA_HIGH = Duration.ofHours(8);
    public static final Duration SLA_MEDIUM = Duration.ofHours(24);
    public static final Duration SLA_LOW = Duration.ofHours(72);

    private static final Map<Priority, Duration> SLA_TARGETS = Map.of(
            Priority.CRITICAL, SLA_CRITICAL,
            Priority.HIGH, SLA_HIGH,
            Priority.MEDIUM, SLA_MEDIUM,
            Priority.LOW, SLA_LOW
    );

    /**
     * Gets the target resolution duration for a given priority.
     */
    public Duration getSlaTargetDuration(Priority priority) {
        if (priority == null) {
            throw new ValidationException("Priority cannot be null");
        }
        return SLA_TARGETS.getOrDefault(priority, SLA_MEDIUM);
    }

    /**
     * Calculates the SLA resolution deadline for a ticket given its creation time.
     */
    public LocalDateTime calculateDeadline(LocalDateTime createdAt, Priority priority) {
        if (createdAt == null) {
            throw new ValidationException("Creation time cannot be null");
        }
        Duration target = getSlaTargetDuration(priority);
        return createdAt.plus(target);
    }

    /**
     * Calculates the actual resolution duration for a resolved or closed ticket.
     */
    public Duration calculateResolutionDuration(Ticket ticket) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null");
        }
        if (ticket.getCreatedAt() == null) {
            throw new ValidationException("Ticket createdAt cannot be null");
        }
        LocalDateTime endTime = ticket.getResolvedAt();
        if (endTime == null) {
            endTime = ticket.getClosedAt();
        }
        if (endTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(ticket.getCreatedAt(), endTime);
    }

    /**
     * Evaluates whether a ticket has violated its SLA.
     */
    public boolean isSlaViolated(Ticket ticket, LocalDateTime currentTime) {
        return calculateSlaStatus(ticket, currentTime) == SlaStatus.VIOLATED;
    }

    /**
     * Computes the SLA status (MET, VIOLATED, or PENDING) for a ticket.
     */
    public SlaStatus calculateSlaStatus(Ticket ticket, LocalDateTime currentTime) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null");
        }
        LocalDateTime now = currentTime != null ? currentTime : LocalDateTime.now();
        LocalDateTime deadline = calculateDeadline(ticket.getCreatedAt(), ticket.getPriority());

        // Cancelled tickets do not hold an active SLA
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            return SlaStatus.PENDING;
        }

        // If ticket was resolved or closed
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            LocalDateTime finishTime = ticket.getResolvedAt() != null ? ticket.getResolvedAt() : ticket.getClosedAt();
            if (finishTime == null) {
                finishTime = ticket.getUpdatedAt();
            }
            return (finishTime.isAfter(deadline)) ? SlaStatus.VIOLATED : SlaStatus.MET;
        }

        // Ticket is currently OPEN, ASSIGNED, or IN_PROGRESS
        if (now.isAfter(deadline)) {
            return SlaStatus.VIOLATED;
        }

        return SlaStatus.PENDING;
    }

    /**
     * Calculates remaining time before SLA violation occurs. Returns negative duration if already overdue.
     */
    public Duration getRemainingTime(Ticket ticket, LocalDateTime currentTime) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null");
        }
        LocalDateTime now = currentTime != null ? currentTime : LocalDateTime.now();
        LocalDateTime deadline = calculateDeadline(ticket.getCreatedAt(), ticket.getPriority());
        return Duration.between(now, deadline);
    }
}
