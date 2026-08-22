package com.fixflow.integration;

import com.fixflow.model.Category;
import com.fixflow.model.Feedback;
import com.fixflow.model.Notification;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.repository.FeedbackRepository;
import com.fixflow.repository.InMemoryFeedbackRepository;
import com.fixflow.repository.InMemoryNotificationRepository;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.InMemoryUserRepository;
import com.fixflow.repository.NotificationRepository;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PBKDF2PasswordHasher;
import com.fixflow.security.PasswordHasher;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.AuthenticationService;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.PriorityService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FixFlow End-to-End Workflow Integration Test")
class FixFlowEndToEndIntegrationTest {

    private UserService userService;
    private AuthenticationService authService;
    private TicketService ticketService;
    private AssignmentService assignmentService;
    private SLAService slaService;
    private NotificationService notificationService;
    private FeedbackService feedbackService;
    private ReportService reportService;

    @BeforeEach
    void setupIntegrationEnvironment() {
        UserRepository userRepository = new InMemoryUserRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();
        FeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();

        PasswordHasher passwordHasher = new PBKDF2PasswordHasher();
        UserValidator userValidator = new UserValidator();
        TicketValidator ticketValidator = new TicketValidator();

        userService = new UserService(userRepository, userValidator, passwordHasher);
        authService = new AuthenticationService(userRepository, passwordHasher, userValidator);
        notificationService = new NotificationService(notificationRepository);
        PriorityService priorityService = new PriorityService();
        slaService = new SLAService();

        ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);
        assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);
        feedbackService = new FeedbackService(feedbackRepository, ticketRepository);
        reportService = new ReportService(ticketRepository, slaService);
    }

    @Test
    @DisplayName("Execute full 10-step lifecycle: Register -> Login -> Create Ticket -> Assign -> In Progress -> Resolve -> SLA -> Close -> Feedback -> Report")
    void shouldExecuteCompleteFixFlowLifecycleSuccessfully() {
        // Step 1: User Registration
        User registeredUser = userService.registerUser(
                "Michael Scott", "mscott", "mscott@dunder.com", "DunderP@ss123", Role.USER);
        User registeredTech = userService.registerUser(
                "Dwight Schrute", "dschrute", "dwight@dunder.com", "BeetsP@ss123", Role.TECHNICIAN);
        User registeredAdmin = userService.registerUser(
                "Jim Halpert", "jhalpert", "jim@dunder.com", "PrankP@ss123", Role.ADMIN);

        assertNotNull(registeredUser.getId());
        assertNotNull(registeredTech.getId());
        assertNotNull(registeredAdmin.getId());

        // Step 2: Authentication
        User loggedInUser = authService.login("mscott", "DunderP@ss123");
        assertEquals(registeredUser.getId(), loggedInUser.getId());

        // Step 3: Create Maintenance Ticket
        Ticket ticket = ticketService.createTicket(
                loggedInUser.getId(),
                "Main Office AC is frozen",
                "Ice buildup on coils causing water leakage near accounting department.",
                Category.FACILITY,
                "Scranton Branch, 2nd Floor",
                Priority.HIGH
        );
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
        assertEquals(Priority.HIGH, ticket.getPriority());

        // Verify ticket creation notification
        List<Notification> userNotifications = notificationService.getUserNotifications(loggedInUser.getId());
        assertFalse(userNotifications.isEmpty());

        // Step 4: Assign Technician by Administrator
        Ticket assigned = assignmentService.assignTechnician(ticket.getId(), registeredTech.getId(), registeredAdmin.getId());
        assertEquals(TicketStatus.ASSIGNED, assigned.getStatus());
        assertEquals(registeredTech.getId(), assigned.getAssignedTechnician().getId());

        // Verify technician received assignment notification
        List<Notification> techNotes = notificationService.getUserNotifications(registeredTech.getId());
        assertFalse(techNotes.isEmpty());

        // Step 5: Start Progress by Technician
        Ticket inProgress = ticketService.startProgress(assigned.getId(), registeredTech.getId());
        assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());

        // Step 6: Resolve Ticket
        Ticket resolved = ticketService.resolveTicket(
                inProgress.getId(), registeredTech.getId(), "Thawed evaporator coils and cleared drain pump.");
        assertEquals(TicketStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // Step 7: Calculate SLA
        SlaStatus sla = slaService.calculateSlaStatus(resolved, null);
        assertEquals(SlaStatus.MET, sla);

        // Step 8: Close Ticket by Reporter
        Ticket closed = ticketService.closeTicket(resolved.getId(), loggedInUser.getId());
        assertEquals(TicketStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());

        // Step 9: User Submits Satisfaction Feedback
        Feedback feedback = feedbackService.submitFeedback(
                closed.getId(), loggedInUser.getId(), 5, "Dwight fixed the AC fast and efficiently!");
        assertEquals(5, feedback.getRating());
        assertEquals(5.0, feedbackService.getAverageRatingForTechnician(registeredTech.getId()));

        // Step 10: Executive Summary Report
        SystemSummaryReport report = reportService.generateSummaryReport();
        assertEquals(1, report.totalTickets());
        assertEquals(0, report.openTickets());
        assertEquals(0, report.inProgressTickets());
        assertEquals(0, report.resolvedTickets());
        assertEquals(1, report.closedTickets());
        assertEquals(1, report.slaMet());
        assertEquals(0, report.slaViolations());
        assertEquals(100.0, report.slaComplianceRatePercentage());
    }
}
