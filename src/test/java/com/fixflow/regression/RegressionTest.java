package com.fixflow.regression;

import com.fixflow.exception.InvalidTicketStatusException;
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
import com.fixflow.service.AssignmentService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.PriorityService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FixFlow Regression Test Suite")
class RegressionTest {

    private UserRepository userRepository;
    private TicketRepository ticketRepository;
    private UserService userService;
    private TicketService ticketService;
    private AssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        ticketRepository = new InMemoryTicketRepository();
        NotificationService notificationService = new NotificationService(new InMemoryNotificationRepository());
        PriorityService priorityService = new PriorityService();
        UserValidator userValidator = new UserValidator();
        TicketValidator ticketValidator = new TicketValidator();

        userService = new UserService(userRepository, userValidator, new PBKDF2PasswordHasher());
        ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);
        assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);
    }

    @Test
    @DisplayName("REGRESSION: Defensively prevent in-memory direct reference modifications on Ticket")
    void shouldPreventDirectExternalMutationOfStoredTickets() {
        User user = userService.registerUser("Jane Doe", "jane_doe", "jane@fixflow.com", "Password123!", Role.USER);
        Ticket ticket = ticketService.createTicket(user.getId(), "Title One", "Description One", Category.HARDWARE, "Room A", Priority.LOW);

        // Mutating the returned object
        ticket.setTitle("Hacked Title");
        ticket.setStatus(TicketStatus.CLOSED);

        // Fetching fresh from repo
        Ticket fresh = ticketService.getTicketById(ticket.getId());
        assertEquals("Title One", fresh.getTitle());
        assertEquals(TicketStatus.OPEN, fresh.getStatus());
    }

    @Test
    @DisplayName("REGRESSION: Case-insensitive email collision prevention on user registration")
    void shouldPreventMixedCaseEmailDuplication() {
        userService.registerUser("User One", "user_one", "user@fixflow.com", "Password123!", Role.USER);

        assertThrows(RuntimeException.class, () ->
                userService.registerUser("User Two", "user_two", "USER@FIXFLOW.COM", "Password123!", Role.USER));
    }

    @Test
    @DisplayName("REGRESSION: Prevent assigning already CLOSED ticket even by Admin")
    void shouldPreventAssigningClosedTicket() {
        User admin = userService.registerUser("Admin", "admin", "admin@fixflow.com", "Password123!", Role.ADMIN);
        User tech = userService.registerUser("Tech", "tech", "tech@fixflow.com", "Password123!", Role.TECHNICIAN);
        User user = userService.registerUser("User", "user", "user3@fixflow.com", "Password123!", Role.USER);

        Ticket ticket = ticketService.createTicket(user.getId(), "Sample Issue", "Sample details here", Category.FACILITY, "Hallway", Priority.LOW);
        assignmentService.assignTechnician(ticket.getId(), tech.getId(), admin.getId());
        ticketService.startProgress(ticket.getId(), tech.getId());
        ticketService.resolveTicket(ticket.getId(), tech.getId(), "Done");
        ticketService.closeTicket(ticket.getId(), user.getId());

        assertThrows(InvalidTicketStatusException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), tech.getId(), admin.getId()));
    }

    @Test
    @DisplayName("REGRESSION: Empty search keyword should return all tickets rather than empty or error")
    void shouldReturnAllTicketsOnEmptySearchQuery() {
        User user = userService.registerUser("User", "user", "user4@fixflow.com", "Password123!", Role.USER);
        ticketService.createTicket(user.getId(), "Issue 1", "Desc 1", Category.HARDWARE, "Loc 1", Priority.LOW);
        ticketService.createTicket(user.getId(), "Issue 2", "Desc 2", Category.SOFTWARE, "Loc 2", Priority.HIGH);

        List<Ticket> emptyQueryResults = ticketService.searchTickets("");
        List<Ticket> nullQueryResults = ticketService.searchTickets(null);
        assertEquals(2, emptyQueryResults.size());
        assertEquals(2, nullQueryResults.size());
    }
}
