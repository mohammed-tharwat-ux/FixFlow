package com.fixflow.service;

import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Feedback;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.repository.FeedbackRepository;
import com.fixflow.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service managing user satisfaction feedback and ratings for completed maintenance tickets.
 */
public class FeedbackService {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    private final FeedbackRepository feedbackRepository;
    private final TicketRepository ticketRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, TicketRepository ticketRepository) {
        this.feedbackRepository = Objects.requireNonNull(feedbackRepository, "feedbackRepository cannot be null");
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository cannot be null");
    }

    /**
     * Submits rating and optional comment for a resolved or closed ticket.
     */
    public Feedback submitFeedback(Long ticketId, Long userId, int rating, String comment) {
        if (ticketId == null) {
            throw new ValidationException("Ticket ID cannot be null");
        }
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new ValidationException("Feedback can only be submitted for RESOLVED or CLOSED tickets (Current status: " + ticket.getStatus() + ")");
        }

        if (ticket.getReporter() == null || !userId.equals(ticket.getReporter().getId())) {
            throw new UnauthorizedOperationException("Only the ticket reporter can submit feedback for this ticket");
        }

        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ValidationException("Rating must be between " + MIN_RATING + " and " + MAX_RATING + " (Received: " + rating + ")");
        }

        if (feedbackRepository.findByTicketId(ticketId).isPresent()) {
            throw new ValidationException("Feedback has already been submitted for Ticket ID #" + ticketId);
        }

        String sanitizedComment = comment != null ? comment.trim() : "";
        if (sanitizedComment.length() > 500) {
            throw new ValidationException("Feedback comment cannot exceed 500 characters");
        }

        Feedback feedback = new Feedback(null, ticketId, userId, rating, sanitizedComment, LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    public Optional<Feedback> getFeedbackForTicket(Long ticketId) {
        if (ticketId == null) {
            throw new ValidationException("Ticket ID cannot be null");
        }
        return feedbackRepository.findByTicketId(ticketId);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public double getAverageRatingForTechnician(Long technicianId) {
        if (technicianId == null) {
            throw new ValidationException("Technician ID cannot be null");
        }
        List<Ticket> techTickets = ticketRepository.findByTechnicianId(technicianId);
        List<Feedback> techFeedbacks = techTickets.stream()
                .map(Ticket::getId)
                .map(feedbackRepository::findByTicketId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (techFeedbacks.isEmpty()) {
            return 0.0;
        }

        return techFeedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
    }
}
