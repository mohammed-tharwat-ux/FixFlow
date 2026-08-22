package com.fixflow.service;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.NotificationType;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Service managing Ticket lifecycle, operations, searching, and transitions.
 */
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketValidator ticketValidator;
    private final NotificationService notificationService;
    private final PriorityService priorityService;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         TicketValidator ticketValidator,
                         NotificationService notificationService,
                         PriorityService priorityService) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.ticketValidator = Objects.requireNonNull(ticketValidator, "ticketValidator cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
        this.priorityService = Objects.requireNonNull(priorityService, "priorityService cannot be null");
    }

    /**
     * Creates a new maintenance ticket.
     */
    public Ticket createTicket(Long reporterId, String title, String description, Category category, String location, Priority explicitPriority) {
        if (reporterId == null) {
            throw new ValidationException("Reporter ID cannot be null");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new UserNotFoundException("Reporter with ID " + reporterId + " not found"));

        if (!reporter.isActive()) {
            throw new UnauthorizedOperationException("Inactive user cannot create maintenance tickets");
        }

        Priority resolvedPriority = priorityService.evaluatePriority(category, title, description, explicitPriority);

        ticketValidator.validateCreation(reporter, title, description, category, location, resolvedPriority);

        Ticket ticket = Ticket.builder()
                .reporter(reporter)
                .title(title.trim())
                .description(description.trim())
                .category(category)
                .location(location.trim())
                .priority(resolvedPriority)
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // Send confirmation notification to reporter
        notificationService.sendNotification(
                reporter.getId(),
                savedTicket.getId(),
                NotificationType.TICKET_CREATED,
                "Ticket Created #" + savedTicket.getId(),
                "Your maintenance ticket '" + savedTicket.getTitle() + "' has been submitted successfully."
        );

        return savedTicket;
    }

    /**
     * Retrieves a ticket by its ID.
     */
    public Ticket getTicketById(Long id) {
        if (id == null) {
            throw new ValidationException("Ticket ID cannot be null");
        }
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with ID " + id + " not found"));
    }

    /**
     * Updates ticket title, description, category, or location.
     */
    public Ticket updateTicket(Long ticketId, Long userId, String title, String description, Category category, String location) {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new InvalidTicketStatusException("Cannot modify a closed or cancelled ticket");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean isReporter = ticket.getReporter() != null && userId.equals(ticket.getReporter().getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isReporter && !isAdmin) {
            throw new UnauthorizedOperationException("Only the reporter or an administrator can update this ticket");
        }

        ticketValidator.validateUpdate(title, description, category, location);

        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setCategory(category);
        ticket.setLocation(location.trim());
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.update(ticket);
    }

    /**
     * Technicians move an assigned ticket to IN_PROGRESS.
     */
    public Ticket startProgress(Long ticketId, Long technicianId) {
        Ticket ticket = getTicketById(ticketId);

        User tech = userRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Technician not found with ID: " + technicianId));

        if (ticket.getAssignedTechnician() == null || !technicianId.equals(ticket.getAssignedTechnician().getId())) {
            if (tech.getRole() != Role.ADMIN) {
                throw new UnauthorizedOperationException("Only the assigned technician can start progress on this ticket");
            }
        }

        ticketValidator.validateStatusTransition(ticket, TicketStatus.IN_PROGRESS);

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updated = ticketRepository.update(ticket);

        if (ticket.getReporter() != null) {
            notificationService.sendNotification(
                    ticket.getReporter().getId(),
                    ticket.getId(),
                    NotificationType.STATUS_CHANGED,
                    "Ticket In Progress #" + ticket.getId(),
                    "Work has started on your ticket by " + tech.getFullName()
            );
        }

        return updated;
    }

    /**
     * Resolves a ticket with technician resolution notes.
     */
    public Ticket resolveTicket(Long ticketId, Long technicianId, String resolutionNotes) {
        if (ValidationUtils.isNullOrBlank(resolutionNotes)) {
            throw new ValidationException("Resolution notes cannot be empty");
        }

        Ticket ticket = getTicketById(ticketId);

        User tech = userRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Technician not found with ID: " + technicianId));

        if (ticket.getAssignedTechnician() == null || !technicianId.equals(ticket.getAssignedTechnician().getId())) {
            if (tech.getRole() != Role.ADMIN) {
                throw new UnauthorizedOperationException("Only the assigned technician can resolve this ticket");
            }
        }

        ticketValidator.validateStatusTransition(ticket, TicketStatus.RESOLVED);

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(now);
        ticket.setResolutionNotes(resolutionNotes.trim());
        ticket.setUpdatedAt(now);

        Ticket updated = ticketRepository.update(ticket);

        if (ticket.getReporter() != null) {
            notificationService.sendNotification(
                    ticket.getReporter().getId(),
                    ticket.getId(),
                    NotificationType.TICKET_RESOLVED,
                    "Ticket Resolved #" + ticket.getId(),
                    "Your ticket has been resolved: " + resolutionNotes.trim()
            );
        }

        return updated;
    }

    /**
     * Closes a resolved ticket.
     */
    public Ticket closeTicket(Long ticketId, Long userId) {
        Ticket ticket = getTicketById(ticketId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean isReporter = ticket.getReporter() != null && userId.equals(ticket.getReporter().getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isReporter && !isAdmin) {
            throw new UnauthorizedOperationException("Only the reporter or an administrator can close this ticket");
        }

        ticketValidator.validateStatusTransition(ticket, TicketStatus.CLOSED);

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(now);
        ticket.setUpdatedAt(now);

        Ticket updated = ticketRepository.update(ticket);

        if (ticket.getAssignedTechnician() != null) {
            notificationService.sendNotification(
                    ticket.getAssignedTechnician().getId(),
                    ticket.getId(),
                    NotificationType.TICKET_CLOSED,
                    "Ticket Closed #" + ticket.getId(),
                    "Ticket #" + ticket.getId() + " has been closed."
            );
        }

        return updated;
    }

    /**
     * Cancels an open or assigned ticket.
     */
    public Ticket cancelTicket(Long ticketId, Long userId, String reason) {
        Ticket ticket = getTicketById(ticketId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean isReporter = ticket.getReporter() != null && userId.equals(ticket.getReporter().getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isReporter && !isAdmin) {
            throw new UnauthorizedOperationException("Only the reporter or an administrator can cancel this ticket");
        }

        ticketValidator.validateStatusTransition(ticket, TicketStatus.CANCELLED);

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setResolutionNotes("Cancelled: " + (reason != null ? reason.trim() : "No reason provided"));
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.update(ticket);
    }

    public List<Ticket> listAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> searchTickets(String keyword) {
        return ticketRepository.search(keyword);
    }

    public List<Ticket> getTicketsByReporter(Long reporterId) {
        if (reporterId == null) {
            throw new ValidationException("Reporter ID cannot be null");
        }
        return ticketRepository.findByReporterId(reporterId);
    }

    public List<Ticket> getTicketsByTechnician(Long technicianId) {
        if (technicianId == null) {
            throw new ValidationException("Technician ID cannot be null");
        }
        return ticketRepository.findByTechnicianId(technicianId);
    }

    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> getTicketsByCategory(Category category) {
        if (category == null) {
            throw new ValidationException("Category cannot be null");
        }
        return ticketRepository.findByCategory(category);
    }

    public List<Ticket> getTicketsByPriority(Priority priority) {
        if (priority == null) {
            throw new ValidationException("Priority cannot be null");
        }
        return ticketRepository.findByPriority(priority);
    }
}
