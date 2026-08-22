package com.fixflow.validation;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates Ticket fields and state transition rules.
 */
public class TicketValidator {

    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 100;

    public static final int DESCRIPTION_MIN_LENGTH = 5;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;

    public static final int LOCATION_MIN_LENGTH = 2;
    public static final int LOCATION_MAX_LENGTH = 100;

    // Allowed status transitions mapping
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, EnumSet.of(TicketStatus.ASSIGNED, TicketStatus.CANCELLED),
            TicketStatus.ASSIGNED, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class),
            TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class)
    );

    /**
     * Validates ticket creation parameters.
     */
    public void validateCreation(User reporter, String title, String description, Category category, String location, Priority priority) {
        if (reporter == null || reporter.getId() == null) {
            throw new ValidationException("Reporter cannot be null and must have a valid ID");
        }
        validateTitle(title);
        validateDescription(description);
        validateCategory(category);
        validateLocation(location);
        validatePriority(priority);
    }

    /**
     * Validates ticket update parameters.
     */
    public void validateUpdate(String title, String description, Category category, String location) {
        validateTitle(title);
        validateDescription(description);
        validateCategory(category);
        validateLocation(location);
    }

    /**
     * Validates title boundary and constraints.
     */
    public void validateTitle(String title) {
        if (ValidationUtils.isNullOrBlank(title)) {
            throw new ValidationException("Ticket title cannot be null or empty");
        }
        String trimmed = title.trim();
        if (trimmed.length() < TITLE_MIN_LENGTH) {
            throw new ValidationException("Ticket title must be at least " + TITLE_MIN_LENGTH + " characters long");
        }
        if (trimmed.length() > TITLE_MAX_LENGTH) {
            throw new ValidationException("Ticket title cannot exceed " + TITLE_MAX_LENGTH + " characters");
        }
    }

    /**
     * Validates description boundary and constraints.
     */
    public void validateDescription(String description) {
        if (ValidationUtils.isNullOrBlank(description)) {
            throw new ValidationException("Ticket description cannot be null or empty");
        }
        String trimmed = description.trim();
        if (trimmed.length() < DESCRIPTION_MIN_LENGTH) {
            throw new ValidationException("Ticket description must be at least " + DESCRIPTION_MIN_LENGTH + " characters long");
        }
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            throw new ValidationException("Ticket description cannot exceed " + DESCRIPTION_MAX_LENGTH + " characters");
        }
    }

    /**
     * Validates location constraints.
     */
    public void validateLocation(String location) {
        if (ValidationUtils.isNullOrBlank(location)) {
            throw new ValidationException("Location cannot be null or empty");
        }
        String trimmed = location.trim();
        if (trimmed.length() < LOCATION_MIN_LENGTH) {
            throw new ValidationException("Location must be at least " + LOCATION_MIN_LENGTH + " characters long");
        }
        if (trimmed.length() > LOCATION_MAX_LENGTH) {
            throw new ValidationException("Location cannot exceed " + LOCATION_MAX_LENGTH + " characters");
        }
    }

    public void validateCategory(Category category) {
        if (category == null) {
            throw new ValidationException("Ticket category cannot be null");
        }
    }

    public void validatePriority(Priority priority) {
        if (priority == null) {
            throw new ValidationException("Ticket priority cannot be null");
        }
    }

    /**
     * Validates that a lifecycle transition from currentStatus to targetStatus is permitted.
     */
    public void validateStatusTransition(Ticket ticket, TicketStatus targetStatus) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null");
        }
        if (targetStatus == null) {
            throw new ValidationException("Target status cannot be null");
        }

        TicketStatus current = ticket.getStatus();
        if (current == targetStatus) {
            return; // No-op transition
        }

        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(TicketStatus.class));
        if (!allowed.contains(targetStatus)) {
            throw new InvalidTicketStatusException(
                    "Invalid ticket status transition from " + current + " to " + targetStatus + " for Ticket ID #" + ticket.getId());
        }
    }
}
