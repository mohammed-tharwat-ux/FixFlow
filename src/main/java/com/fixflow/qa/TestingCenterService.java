package com.fixflow.qa;

import com.fixflow.exception.AuthenticationException;
import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UserAlreadyExistsException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing QA Testing Center operations, live negative test execution,
 * test specifications, and QA summary reporting.
 */
public class TestingCenterService {

    /**
     * Executes real negative testing scenarios against fresh isolated service instances
     * and records actual runtime outputs and pass/fail states.
     */
    public List<NegativeTestScenarioResult> runNegativeScenarios() {
        List<NegativeTestScenarioResult> results = new ArrayList<>();

        // Create isolated test environment
        UserRepository userRepository = new InMemoryUserRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();
        FeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();

        PasswordHasher passwordHasher = new PBKDF2PasswordHasher();
        UserValidator userValidator = new UserValidator();
        TicketValidator ticketValidator = new TicketValidator();

        UserService userService = new UserService(userRepository, userValidator, passwordHasher);
        AuthenticationService authService = new AuthenticationService(userRepository, passwordHasher, userValidator);
        NotificationService notificationService = new NotificationService(notificationRepository);
        PriorityService priorityService = new PriorityService();
        SLAService slaService = new SLAService();
        TicketService ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);
        AssignmentService assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);
        FeedbackService feedbackService = new FeedbackService(feedbackRepository, ticketRepository);

        // Seed basic users
        User reporter = userService.registerUser("John Reporter", "john_rep", "john.rep@fixflow.com", "Password123!", Role.USER);
        User tech = userService.registerUser("Bob Tech", "bob_tech", "bob.tech@fixflow.com", "Password123!", Role.TECHNICIAN);
        User admin = userService.registerUser("Sarah Admin", "sarah_admin", "sarah.admin@fixflow.com", "Password123!", Role.ADMIN);

        // Scenario 1: Login with incorrect password
        try {
            authService.login("john_rep", "WrongPassword999!");
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-001",
                    "Login With Incorrect Password",
                    "Attempt to authenticate using a valid username with an incorrect password.",
                    "AuthenticationException (Invalid username or password)",
                    "Login unexpectedly succeeded without throwing exception",
                    false
            ));
        } catch (AuthenticationException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-001",
                    "Login With Incorrect Password",
                    "Attempt to authenticate using a valid username with an incorrect password.",
                    "AuthenticationException (Invalid username or password)",
                    "AuthenticationException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-001",
                    "Login With Incorrect Password",
                    "Attempt to authenticate using a valid username with an incorrect password.",
                    "AuthenticationException",
                    "Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                    false
            ));
        }

        // Scenario 2: Register duplicate username
        try {
            userService.registerUser("Another John", "john_rep", "other.email@fixflow.com", "Password123!", Role.USER);
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-002",
                    "Register Duplicate Username",
                    "Attempt to register a new user with an existing taken username.",
                    "UserAlreadyExistsException",
                    "Registration succeeded with duplicate username",
                    false
            ));
        } catch (UserAlreadyExistsException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-002",
                    "Register Duplicate Username",
                    "Attempt to register a new user with an existing taken username.",
                    "UserAlreadyExistsException",
                    "UserAlreadyExistsException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-002",
                    "Register Duplicate Username",
                    "Attempt to register a new user with an existing taken username.",
                    "UserAlreadyExistsException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 3: Create ticket with empty title
        try {
            ticketService.createTicket(reporter.getId(), "   ", "Valid description here", Category.HARDWARE, "Room 101", Priority.LOW);
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-003",
                    "Create Ticket With Empty Title",
                    "Attempt to create a ticket with whitespace/empty title.",
                    "ValidationException (Ticket title cannot be null or empty)",
                    "Ticket created with empty title",
                    false
            ));
        } catch (ValidationException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-003",
                    "Create Ticket With Empty Title",
                    "Attempt to create a ticket with whitespace/empty title.",
                    "ValidationException (Ticket title cannot be null or empty)",
                    "ValidationException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-003",
                    "Create Ticket With Empty Title",
                    "Attempt to create a ticket with whitespace/empty title.",
                    "ValidationException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 4: Create ticket with invalid short description
        try {
            ticketService.createTicket(reporter.getId(), "Valid Title", "Tiny", Category.HARDWARE, "Room 101", Priority.LOW);
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-004",
                    "Create Ticket With Short Description (< 5 chars)",
                    "Attempt to create a ticket with description of 4 chars (below min boundary 5).",
                    "ValidationException (must be at least 5 characters long)",
                    "Ticket created with invalid short description",
                    false
            ));
        } catch (ValidationException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-004",
                    "Create Ticket With Short Description (< 5 chars)",
                    "Attempt to create a ticket with description of 4 chars (below min boundary 5).",
                    "ValidationException (must be at least 5 characters long)",
                    "ValidationException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-004",
                    "Create Ticket With Short Description (< 5 chars)",
                    "Attempt to create a ticket with description of 4 chars (below min boundary 5).",
                    "ValidationException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 5: Request non-existent ticket
        try {
            ticketService.getTicketById(99999L);
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-005",
                    "Request Non-Existent Ticket",
                    "Attempt to fetch a ticket by ID that does not exist in repository.",
                    "TicketNotFoundException (Ticket with ID 99999 not found)",
                    "Returned non-existent ticket without error",
                    false
            ));
        } catch (TicketNotFoundException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-005",
                    "Request Non-Existent Ticket",
                    "Attempt to fetch a ticket by ID that does not exist in repository.",
                    "TicketNotFoundException (Ticket with ID 99999 not found)",
                    "TicketNotFoundException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-005",
                    "Request Non-Existent Ticket",
                    "Attempt to fetch a ticket by ID that does not exist in repository.",
                    "TicketNotFoundException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 6: Assign inactive technician
        User inactiveTech = userService.registerUser("Inactive Bob", "inactive_bob", "inact@fixflow.com", "Password123!", Role.TECHNICIAN);
        userService.deactivateUser(inactiveTech.getId());
        Ticket t1 = ticketService.createTicket(reporter.getId(), "Projector Broken", "Projector lamp failed in room 201", Category.HARDWARE, "Room 201", Priority.MEDIUM);
        try {
            assignmentService.assignTechnician(t1.getId(), inactiveTech.getId(), admin.getId());
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-006",
                    "Assign Inactive Technician",
                    "Attempt to assign ticket to a technician whose account is INACTIVE.",
                    "ValidationException (Cannot assign ticket to an inactive technician account)",
                    "Assignment succeeded to inactive technician",
                    false
            ));
        } catch (ValidationException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-006",
                    "Assign Inactive Technician",
                    "Attempt to assign ticket to a technician whose account is INACTIVE.",
                    "ValidationException (Cannot assign ticket to an inactive technician account)",
                    "ValidationException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-006",
                    "Assign Inactive Technician",
                    "Attempt to assign ticket to a technician whose account is INACTIVE.",
                    "ValidationException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 7: Assign non-existent technician
        try {
            assignmentService.assignTechnician(t1.getId(), 88888L, admin.getId());
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-007",
                    "Assign Non-Existent Technician",
                    "Attempt to assign ticket to a technician ID that does not exist.",
                    "UserNotFoundException (Technician not found with ID: 88888)",
                    "Assignment succeeded with non-existent technician",
                    false
            ));
        } catch (UserNotFoundException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-007",
                    "Assign Non-Existent Technician",
                    "Attempt to assign ticket to a technician ID that does not exist.",
                    "UserNotFoundException (Technician not found with ID: 88888)",
                    "UserNotFoundException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-007",
                    "Assign Non-Existent Technician",
                    "Attempt to assign ticket to a technician ID that does not exist.",
                    "UserNotFoundException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 8: Invalid ticket status transition (CLOSED -> IN_PROGRESS)
        assignmentService.assignTechnician(t1.getId(), tech.getId(), admin.getId());
        ticketService.startProgress(t1.getId(), tech.getId());
        ticketService.resolveTicket(t1.getId(), tech.getId(), "Replaced lamp bulb");
        ticketService.closeTicket(t1.getId(), reporter.getId());
        try {
            ticketService.startProgress(t1.getId(), tech.getId());
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-008",
                    "Invalid Ticket Status Transition (CLOSED -> IN_PROGRESS)",
                    "Attempt to restart progress on an already CLOSED ticket.",
                    "InvalidTicketStatusException (Invalid ticket status transition from CLOSED to IN_PROGRESS)",
                    "Transition allowed on closed ticket",
                    false
            ));
        } catch (InvalidTicketStatusException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-008",
                    "Invalid Ticket Status Transition (CLOSED -> IN_PROGRESS)",
                    "Attempt to restart progress on an already CLOSED ticket.",
                    "InvalidTicketStatusException (Invalid ticket status transition from CLOSED to IN_PROGRESS)",
                    "InvalidTicketStatusException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-008",
                    "Invalid Ticket Status Transition (CLOSED -> IN_PROGRESS)",
                    "Attempt to restart progress on an already CLOSED ticket.",
                    "InvalidTicketStatusException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 9: Close ticket before resolution (OPEN -> CLOSED)
        Ticket openTicket = ticketService.createTicket(reporter.getId(), "WiFi slow in library", "Experiencing low signal in reading area", Category.NETWORK, "Library 1st Floor", Priority.LOW);
        try {
            ticketService.closeTicket(openTicket.getId(), reporter.getId());
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-009",
                    "Close Ticket Before Resolution",
                    "Attempt to close an unresolved OPEN ticket directly.",
                    "InvalidTicketStatusException (Invalid ticket status transition from OPEN to CLOSED)",
                    "OPEN ticket closed without resolution",
                    false
            ));
        } catch (InvalidTicketStatusException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-009",
                    "Close Ticket Before Resolution",
                    "Attempt to close an unresolved OPEN ticket directly.",
                    "InvalidTicketStatusException (Invalid ticket status transition from OPEN to CLOSED)",
                    "InvalidTicketStatusException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-009",
                    "Close Ticket Before Resolution",
                    "Attempt to close an unresolved OPEN ticket directly.",
                    "InvalidTicketStatusException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        // Scenario 10: Submit feedback with invalid rating
        try {
            feedbackService.submitFeedback(t1.getId(), reporter.getId(), 7, "Beyond 5 stars");
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-010",
                    "Submit Feedback With Invalid Rating (> 5 stars)",
                    "Attempt to submit feedback with rating of 7 (valid range: 1–5).",
                    "ValidationException (Rating must be between 1 and 5)",
                    "Feedback submitted with invalid rating 7",
                    false
            ));
        } catch (ValidationException e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-010",
                    "Submit Feedback With Invalid Rating (> 5 stars)",
                    "Attempt to submit feedback with rating of 7 (valid range: 1–5).",
                    "ValidationException (Rating must be between 1 and 5)",
                    "ValidationException: " + e.getMessage(),
                    true
            ));
        } catch (Exception e) {
            results.add(new NegativeTestScenarioResult(
                    "TC-NEG-010",
                    "Submit Feedback With Invalid Rating (> 5 stars)",
                    "Attempt to submit feedback with rating of 7 (valid range: 1–5).",
                    "ValidationException",
                    "Unexpected exception: " + e.getClass().getSimpleName(),
                    false
            ));
        }

        return results;
    }
}
