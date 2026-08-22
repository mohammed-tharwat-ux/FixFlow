package com.fixflow.service;

import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Feedback;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.repository.FeedbackRepository;
import com.fixflow.repository.InMemoryFeedbackRepository;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackService Unit & Boundary Tests")
class FeedbackServiceTest {

    private FeedbackRepository feedbackRepository;
    private TicketRepository ticketRepository;
    private FeedbackService feedbackService;

    private User reporter;
    private User technician;
    private User otherUser;
    private Ticket closedTicket;
    private Ticket openTicket;

    @BeforeEach
    void setUp() {
        feedbackRepository = new InMemoryFeedbackRepository();
        ticketRepository = new InMemoryTicketRepository();
        feedbackService = new FeedbackService(feedbackRepository, ticketRepository);

        reporter = User.builder().id(1L).fullName("Reporter").username("reporter").email("rep@fixflow.com").role(Role.USER).build();
        technician = User.builder().id(2L).fullName("Technician").username("tech").email("tech@fixflow.com").role(Role.TECHNICIAN).build();
        otherUser = User.builder().id(3L).fullName("Other").username("other").email("other@fixflow.com").role(Role.USER).build();

        closedTicket = ticketRepository.save(Ticket.builder()
                .reporter(reporter)
                .assignedTechnician(technician)
                .title("Broken Chair")
                .description("Fixed hydraulic piston")
                .category(Category.FACILITY)
                .location("Room 101")
                .priority(Priority.LOW)
                .status(TicketStatus.CLOSED)
                .build());

        openTicket = ticketRepository.save(Ticket.builder()
                .reporter(reporter)
                .title("Network outage")
                .description("No internet")
                .category(Category.NETWORK)
                .location("Floor 2")
                .priority(Priority.HIGH)
                .status(TicketStatus.OPEN)
                .build());
    }

    @Nested
    @DisplayName("Rating Boundary Tests (1 to 5 Stars)")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Boundary: min-1 (0 stars) -> invalid")
        void shouldRejectRatingBelowMin() {
            assertThrows(ValidationException.class, () ->
                    feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 0, "Too low"));
        }

        @Test
        @DisplayName("Boundary: min (1 star) -> valid")
        void shouldAcceptRatingAtMin() {
            Feedback f = feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 1, "Poor service");
            assertEquals(1, f.getRating());
        }

        @Test
        @DisplayName("Boundary: max (5 stars) -> valid")
        void shouldAcceptRatingAtMax() {
            Feedback f = feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 5, "Outstanding service!");
            assertEquals(5, f.getRating());
        }

        @Test
        @DisplayName("Boundary: max+1 (6 stars) -> invalid")
        void shouldRejectRatingAboveMax() {
            assertThrows(ValidationException.class, () ->
                    feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 6, "Too high"));
        }
    }

    @Nested
    @DisplayName("Business & Authorization Rules")
    class BusinessRulesTests {

        @Test
        @DisplayName("Reject feedback on OPEN or UNRESOLVED tickets")
        void shouldRejectFeedbackOnOpenTicket() {
            ValidationException ex = assertThrows(ValidationException.class, () ->
                    feedbackService.submitFeedback(openTicket.getId(), reporter.getId(), 5, "Great"));
            assertTrue(ex.getMessage().contains("only be submitted for RESOLVED or CLOSED"));
        }

        @Test
        @DisplayName("Reject feedback from non-reporter user")
        void shouldRejectFeedbackFromUnauthorizedUser() {
            assertThrows(UnauthorizedOperationException.class, () ->
                    feedbackService.submitFeedback(closedTicket.getId(), otherUser.getId(), 5, "Good"));
        }

        @Test
        @DisplayName("Reject duplicate feedback on same ticket")
        void shouldRejectDuplicateFeedback() {
            feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 4, "Initial feedback");

            assertThrows(ValidationException.class, () ->
                    feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 5, "Second feedback"));
        }

        @Test
        @DisplayName("Calculate technician average rating")
        void shouldCalculateTechnicianAverageRating() {
            Ticket secondClosed = ticketRepository.save(Ticket.builder()
                    .reporter(reporter)
                    .assignedTechnician(technician)
                    .title("Monitor fixed")
                    .description("Power cord replaced")
                    .category(Category.HARDWARE)
                    .location("Room 102")
                    .priority(Priority.LOW)
                    .status(TicketStatus.CLOSED)
                    .build());

            feedbackService.submitFeedback(closedTicket.getId(), reporter.getId(), 4, "Good");
            feedbackService.submitFeedback(secondClosed.getId(), reporter.getId(), 5, "Excellent");

            double avg = feedbackService.getAverageRatingForTechnician(technician.getId());
            assertEquals(4.5, avg);
        }
    }
}
