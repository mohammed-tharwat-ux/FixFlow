package com.fixflow.service;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import com.fixflow.repository.InMemoryNotificationRepository;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.InMemoryUserRepository;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PBKDF2PasswordHasher;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketService Tests")
class TicketServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private PriorityService priorityService;
    private TicketService ticketService;

    private User reporter;
    private User technician;
    private User admin;
    private User otherUser;

    @BeforeEach
    void setUp() {
        ticketRepository = new InMemoryTicketRepository();
        userRepository = new InMemoryUserRepository();
        notificationService = new NotificationService(new InMemoryNotificationRepository());
        priorityService = new PriorityService();
        TicketValidator ticketValidator = new TicketValidator();

        ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);

        reporter = userRepository.save(User.builder().fullName("John Reporter").username("john").email("john@fixflow.com").role(Role.USER).status(UserStatus.ACTIVE).build());
        technician = userRepository.save(User.builder().fullName("Bob Technician").username("bob").email("bob@fixflow.com").role(Role.TECHNICIAN).status(UserStatus.ACTIVE).build());
        admin = userRepository.save(User.builder().fullName("Sarah Admin").username("admin").email("admin@fixflow.com").role(Role.ADMIN).status(UserStatus.ACTIVE).build());
        otherUser = userRepository.save(User.builder().fullName("Other User").username("other").email("other@fixflow.com").role(Role.USER).status(UserStatus.ACTIVE).build());
    }

    @Nested
    @DisplayName("Ticket Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Create ticket successfully with explicit priority")
        void shouldCreateTicketWithExplicitPriority() {
            Ticket ticket = ticketService.createTicket(
                    reporter.getId(),
                    "Broken AC in Lab",
                    "AC unit not cooling and making loud rattling noise",
                    Category.FACILITY,
                    "Lab 204",
                    Priority.HIGH
            );

            assertNotNull(ticket.getId());
            assertEquals("Broken AC in Lab", ticket.getTitle());
            assertEquals(Priority.HIGH, ticket.getPriority());
            assertEquals(TicketStatus.OPEN, ticket.getStatus());
            assertEquals(reporter.getId(), ticket.getReporter().getId());
            assertNotNull(ticket.getCreatedAt());
        }

        @Test
        @DisplayName("Create ticket with automatic priority evaluation based on keywords")
        void shouldEvaluateCriticalPriorityForOutageKeyword() {
            Ticket ticket = ticketService.createTicket(
                    reporter.getId(),
                    "Major server room power outage",
                    "Complete blackout across primary power grid",
                    Category.ELECTRICAL,
                    "Server Room B",
                    null
            );

            assertEquals(Priority.CRITICAL, ticket.getPriority());
        }

        @Test
        @DisplayName("Reject ticket creation for inactive user")
        void shouldRejectCreationForInactiveUser() {
            reporter.setStatus(UserStatus.INACTIVE);
            userRepository.update(reporter);

            assertThrows(UnauthorizedOperationException.class, () ->
                    ticketService.createTicket(reporter.getId(), "Title", "Valid description here", Category.OTHER, "Room 1", Priority.LOW));
        }

        @Test
        @DisplayName("Reject ticket creation with invalid inputs")
        void shouldRejectCreationWithInvalidInputs() {
            assertThrows(ValidationException.class, () ->
                    ticketService.createTicket(null, "Title", "Description", Category.OTHER, "Room 1", Priority.LOW));

            assertThrows(ValidationException.class, () ->
                    ticketService.createTicket(reporter.getId(), "No", "Description", Category.OTHER, "Room 1", Priority.LOW));
        }
    }

    @Nested
    @DisplayName("Lifecycle Operations & Transitions")
    class LifecycleTests {

        private Ticket ticket;

        @BeforeEach
        void createInitialTicket() {
            ticket = ticketService.createTicket(
                    reporter.getId(),
                    "Projector failure",
                    "Lamp burned out during lecture",
                    Category.HARDWARE,
                    "Auditorium 1",
                    Priority.MEDIUM
            );
            ticket.setAssignedTechnician(technician);
            ticket.setStatus(TicketStatus.ASSIGNED);
            ticketRepository.update(ticket);
        }

        @Test
        @DisplayName("Technician starts progress on assigned ticket")
        void shouldStartProgressSuccessfully() {
            Ticket inProgress = ticketService.startProgress(ticket.getId(), technician.getId());
            assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());
        }

        @Test
        @DisplayName("Unauthorized technician cannot start progress")
        void shouldRejectStartProgressByUnauthorizedUser() {
            assertThrows(UnauthorizedOperationException.class, () ->
                    ticketService.startProgress(ticket.getId(), otherUser.getId()));
        }

        @Test
        @DisplayName("Resolve ticket with resolution notes")
        void shouldResolveTicketSuccessfully() {
            ticketService.startProgress(ticket.getId(), technician.getId());
            Ticket resolved = ticketService.resolveTicket(ticket.getId(), technician.getId(), "Replaced projector lamp with OEM bulb.");

            assertEquals(TicketStatus.RESOLVED, resolved.getStatus());
            assertNotNull(resolved.getResolvedAt());
            assertEquals("Replaced projector lamp with OEM bulb.", resolved.getResolutionNotes());
        }

        @Test
        @DisplayName("Close resolved ticket by reporter")
        void shouldCloseResolvedTicket() {
            ticketService.startProgress(ticket.getId(), technician.getId());
            ticketService.resolveTicket(ticket.getId(), technician.getId(), "Resolved issue.");

            Ticket closed = ticketService.closeTicket(ticket.getId(), reporter.getId());
            assertEquals(TicketStatus.CLOSED, closed.getStatus());
            assertNotNull(closed.getClosedAt());
        }

        @Test
        @DisplayName("Closing un-resolved ticket directly throws InvalidTicketStatusException")
        void shouldRejectClosingOpenOrAssignedTicket() {
            assertThrows(InvalidTicketStatusException.class, () ->
                    ticketService.closeTicket(ticket.getId(), reporter.getId()));
        }

        @Test
        @DisplayName("Cancel ticket by reporter")
        void shouldCancelTicket() {
            Ticket cancelled = ticketService.cancelTicket(ticket.getId(), reporter.getId(), "No longer needed");
            assertEquals(TicketStatus.CANCELLED, cancelled.getStatus());
        }
    }

    @Nested
    @DisplayName("Search & Filtering Tests")
    class QueryAndSearchTests {

        @BeforeEach
        void createSampleTickets() {
            ticketService.createTicket(reporter.getId(), "Network router offline", "Cannot connect to WiFi", Category.NETWORK, "Floor 1", Priority.HIGH);
            ticketService.createTicket(reporter.getId(), "Monitor display flickering", "HDMI port faulty", Category.HARDWARE, "Floor 2", Priority.LOW);
            ticketService.createTicket(otherUser.getId(), "Water leak near printer", "Ceiling pipe leaking", Category.FACILITY, "Floor 1", Priority.CRITICAL);
        }

        @Test
        void shouldSearchByKeyword() {
            List<Ticket> results = ticketService.searchTickets("WiFi");
            assertEquals(1, results.size());
            assertEquals("Network router offline", results.get(0).getTitle());

            List<Ticket> floor1 = ticketService.searchTickets("Floor 1");
            assertEquals(2, floor1.size());
        }

        @Test
        void shouldFilterByStatusAndCategory() {
            assertEquals(3, ticketService.getTicketsByStatus(TicketStatus.OPEN).size());
            assertEquals(1, ticketService.getTicketsByCategory(Category.NETWORK).size());
            assertEquals(1, ticketService.getTicketsByPriority(Priority.CRITICAL).size());
            assertEquals(2, ticketService.getTicketsByReporter(reporter.getId()).size());
        }
    }
}
