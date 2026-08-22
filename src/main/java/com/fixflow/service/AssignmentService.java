package com.fixflow.service;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.NotificationType;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Service managing technician assignment to maintenance tickets.
 */
public class AssignmentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AssignmentService(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService cannot be null");
    }

    /**
     * Assigns a qualified active technician to a ticket.
     *
     * @param ticketId      the ID of the ticket to assign
     * @param technicianId  the ID of the technician
     * @param assignedById  the ID of the user performing the assignment (Admin/Supervisor)
     * @return the updated ticket with technician assigned
     */
    public Ticket assignTechnician(Long ticketId, Long technicianId, Long assignedById) {
        if (ticketId == null) {
            throw new ValidationException("Ticket ID cannot be null");
        }
        if (technicianId == null) {
            throw new ValidationException("Technician ID cannot be null");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new InvalidTicketStatusException("Cannot assign technician to a " + ticket.getStatus() + " ticket");
        }

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Technician not found with ID: " + technicianId));

        if (technician.getRole() != Role.TECHNICIAN && technician.getRole() != Role.ADMIN) {
            throw new ValidationException("Assigned user must possess the TECHNICIAN or ADMIN role (Found: " + technician.getRole() + ")");
        }

        if (!technician.isActive()) {
            throw new ValidationException("Cannot assign ticket to an inactive technician account");
        }

        if (assignedById != null) {
            User assigner = userRepository.findById(assignedById)
                    .orElseThrow(() -> new UserNotFoundException("Assigner not found with ID: " + assignedById));
            if (assigner.getRole() != Role.ADMIN && !assigner.getId().equals(technicianId)) {
                throw new UnauthorizedOperationException("Only administrators can assign tickets to technicians");
            }
        }

        ticket.setAssignedTechnician(technician);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updated = ticketRepository.update(ticket);

        // Notify assigned technician
        notificationService.sendNotification(
                technician.getId(),
                ticket.getId(),
                NotificationType.TECHNICIAN_ASSIGNED,
                "New Ticket Assigned #" + ticket.getId(),
                "You have been assigned to maintenance ticket: " + ticket.getTitle() + " (" + ticket.getPriority() + ")"
        );

        // Notify reporter
        if (ticket.getReporter() != null) {
            notificationService.sendNotification(
                    ticket.getReporter().getId(),
                    ticket.getId(),
                    NotificationType.STATUS_CHANGED,
                    "Technician Assigned #" + ticket.getId(),
                    "Technician " + technician.getFullName() + " has been assigned to your ticket."
            );
        }

        return updated;
    }

    /**
     * Retrieves all available active technicians.
     */
    public List<User> getAvailableTechnicians() {
        return userRepository.findByRole(Role.TECHNICIAN).stream()
                .filter(User::isActive)
                .toList();
    }

    /**
     * Calculates the active workload (count of open/assigned/in-progress tickets) for a technician.
     */
    public long getTechnicianActiveWorkload(Long technicianId) {
        if (technicianId == null) {
            throw new ValidationException("Technician ID cannot be null");
        }
        return ticketRepository.findByTechnicianId(technicianId).stream()
                .filter(t -> t.getStatus() == TicketStatus.ASSIGNED || t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
    }
}
