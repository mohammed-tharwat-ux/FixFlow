package com.fixflow.ui;

import com.fixflow.data.DemoDataLoader;
import com.fixflow.exception.AuthenticationException;
import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.UserAlreadyExistsException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
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
import com.fixflow.qa.TestingCenterService;
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
import com.fixflow.ui.views.DashboardView;
import com.fixflow.ui.views.ProfileSettingsView;
import com.fixflow.ui.views.ReportsAnalyticsView;
import com.fixflow.ui.views.SlaMonitoringView;
import com.fixflow.ui.views.TechnicianWorkspaceView;
import com.fixflow.ui.views.TestingCenterView;
import com.fixflow.ui.views.TicketView;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.UserValidator;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application Shell, Unified Navigation, Presentation Runner, and Demo Dispatcher for FixFlow.
 */
public class FixFlowApp {

    private final UserService userService;
    private final AuthenticationService authService;
    private final TicketService ticketService;
    private final AssignmentService assignmentService;
    private final PriorityService priorityService;
    private final SLAService slaService;
    private final NotificationService notificationService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;
    private final TestingCenterService testingCenterService;

    // View Components
    private final DashboardView dashboardView;
    private final TicketView ticketView;
    private final TechnicianWorkspaceView techWorkspaceView;
    private final SlaMonitoringView slaMonitoringView;
    private final ReportsAnalyticsView reportsAnalyticsView;
    private final TestingCenterView testingCenterView;
    private final ProfileSettingsView profileSettingsView;
    private final PresentationMode presentationMode;

    private User currentUser = null;
    private final Scanner scanner;

    public FixFlowApp() {
        this.scanner = new Scanner(System.in);

        UserRepository userRepository = new InMemoryUserRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();
        FeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();

        PasswordHasher passwordHasher = new PBKDF2PasswordHasher();
        UserValidator userValidator = new UserValidator();
        TicketValidator ticketValidator = new TicketValidator();

        this.userService = new UserService(userRepository, userValidator, passwordHasher);
        this.authService = new AuthenticationService(userRepository, passwordHasher, userValidator);
        this.notificationService = new NotificationService(notificationRepository);
        this.priorityService = new PriorityService();
        this.slaService = new SLAService();
        this.ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);
        this.assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);
        this.feedbackService = new FeedbackService(feedbackRepository, ticketRepository);
        this.reportService = new ReportService(ticketRepository, slaService);
        this.testingCenterService = new TestingCenterService();

        this.dashboardView = new DashboardView(ticketService, userService, assignmentService, reportService, slaService);
        this.ticketView = new TicketView(ticketService, slaService, feedbackService, notificationService);
        this.techWorkspaceView = new TechnicianWorkspaceView(ticketService, slaService);
        this.slaMonitoringView = new SlaMonitoringView(ticketService, slaService);
        this.reportsAnalyticsView = new ReportsAnalyticsView(reportService, ticketService);
        this.testingCenterView = new TestingCenterView(testingCenterService);
        this.profileSettingsView = new ProfileSettingsView();
        this.presentationMode = new PresentationMode(userService, authService, ticketService, assignmentService, slaService, notificationService, feedbackService, reportService);

        // Preload demo seed data
        DemoDataLoader.loadDemoData(userService, ticketService, assignmentService, slaService);
    }

    public static void main(String[] args) {
        FixFlowApp app = new FixFlowApp();

        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            app.runAutomatedDemoScenario();
            return;
        }

        if (System.console() == null && args.length == 0) {
            app.runAutomatedDemoScenario();
        } else {
            PresentationIntro.displaySplashScreen(app.scanner);
            app.runMainLoop();
        }
    }

    // =========================================================================
    // MAIN APPLICATION NAVIGATION LOOP
    // =========================================================================

    public void runMainLoop() {
        boolean running = true;
        while (running) {
            if (currentUser == null) {
                printMainMenuHeader();
                System.out.println("  1. Login to FixFlow Account");
                System.out.println("  2. Register New User Account");
                System.out.println("  3. QA & Software Testing Center");
                System.out.println("  4. Guided Presentation Mode (11 Steps)");
                System.out.println("  5. Run Full Automated Demo (--demo)");
                System.out.println("  6. Exit");
                System.out.print("\n  Select an option (1-6): ");
                String choice = readInput();

                switch (choice) {
                    case "1" -> handleLogin();
                    case "2" -> handleRegistration();
                    case "3" -> testingCenterView.renderMenu(scanner);
                    case "4" -> presentationMode.runGuidedPresentation(scanner);
                    case "5" -> runAutomatedDemoScenario();
                    case "6" -> {
                        System.out.println("\n  Thank you for using FixFlow. Session terminated.");
                        running = false;
                    }
                    default -> System.out.println("\n  [ERROR] Invalid option. Please enter a number from 1 to 6.");
                }
            } else {
                switch (currentUser.getRole()) {
                    case USER -> runUserShell();
                    case ADMIN -> runAdminShell();
                    case TECHNICIAN -> runTechnicianShell();
                }
            }
        }
    }

    private void printMainMenuHeader() {
        ConsoleTheme.printHeader("FIXFLOW - SMART MAINTENANCE SYSTEM");
    }

    // =========================================================================
    // AUTHENTICATION & REGISTRATION
    // =========================================================================

    private void handleLogin() {
        ConsoleTheme.printSection("USER AUTHENTICATION");
        System.out.print("  Username: ");
        String username = readInput();
        System.out.print("  Password: ");
        String password = readInput();

        try {
            currentUser = authService.login(username, password);
            System.out.printf("\n  [SUCCESS] Authenticated successfully as: %s [%s]\n",
                    currentUser.getFullName(), currentUser.getRole());
        } catch (AuthenticationException e) {
            System.out.println("\n  [FAILED] Login failed:\n  " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  [ERROR] Login encountered an issue: " + e.getMessage());
        }
    }

    private void handleRegistration() {
        ConsoleTheme.printSection("CREATE NEW USER ACCOUNT");
        System.out.print("  Full Name: ");
        String fullName = readInput();
        System.out.print("  Username:  ");
        String username = readInput();
        System.out.print("  Email:     ");
        String email = readInput();
        System.out.print("  Password:  ");
        String password = readInput();

        try {
            User registered = userService.registerUser(fullName, username, email, password, Role.USER);
            System.out.println("\n  [SUCCESS] Account successfully registered!");
            System.out.printf("  User ID: #%d | Username: %s | Role: %s\n",
                    registered.getId(), registered.getUsername(), registered.getRole());
            System.out.println("  You can now login using your credentials.");
        } catch (ValidationException e) {
            System.out.println("\n  [FAILED] Registration Validation Error:\n  " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("\n  [FAILED] Registration Conflict:\n  " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  [ERROR] Registration error: " + e.getMessage());
        }
    }

    // =========================================================================
    // ROLE APPLICATION SHELLS
    // =========================================================================

    private void runUserShell() {
        ConsoleTheme.printAppShell(currentUser, "User Portal");
        System.out.println("  1. Dashboard Overview");
        System.out.println("  2. Create Maintenance Request");
        System.out.println("  3. View My Submitted Tickets");
        System.out.println("  4. Inspect Ticket by ID");
        System.out.println("  5. View In-System Notifications");
        System.out.println("  6. Submit Satisfaction Feedback");
        System.out.println("  7. View Analytics & Reports");
        System.out.println("  8. Profile & Account Settings");
        System.out.println("  9. Logout");
        System.out.print("\n  Select an option (1-9): ");
        String choice = readInput();

        switch (choice) {
            case "1" -> dashboardView.render(currentUser);
            case "2" -> handleCreateTicketPrompt();
            case "3" -> ticketView.renderTicketList(ticketService.getTicketsByReporter(currentUser.getId()), "My Reported Tickets");
            case "4" -> handleInspectTicketPrompt();
            case "5" -> handleViewNotifications();
            case "6" -> handleSubmitFeedbackPrompt();
            case "7" -> reportsAnalyticsView.render(currentUser);
            case "8" -> handleProfileAndSettings();
            case "9" -> {
                System.out.println("\n  Logged out successfully.");
                currentUser = null;
            }
            default -> System.out.println("\n  [ERROR] Invalid option. Please select 1-9.");
        }
    }

    private void runAdminShell() {
        ConsoleTheme.printAppShell(currentUser, "Administrator Operations Center");
        System.out.println("  1. Dashboard Overview & KPIs");
        System.out.println("  2. View All Maintenance Tickets");
        System.out.println("  3. Inspect Ticket by ID");
        System.out.println("  4. Assign Technician to Ticket");
        System.out.println("  5. SLA Compliance Monitoring");
        System.out.println("  6. Directory of Registered Users");
        System.out.println("  7. Executive Reports & Analytics");
        System.out.println("  8. QA & Software Testing Center");
        System.out.println("  9. Profile & System Settings");
        System.out.println(" 10. Logout");
        System.out.print("\n  Select an option (1-10): ");
        String choice = readInput();

        switch (choice) {
            case "1" -> dashboardView.render(currentUser);
            case "2" -> ticketView.renderTicketList(ticketService.listAllTickets(), "All System Incidents");
            case "3" -> handleInspectTicketPrompt();
            case "4" -> handleAssignTechnicianPrompt();
            case "5" -> slaMonitoringView.render(currentUser);
            case "6" -> handleViewUsers();
            case "7" -> reportsAnalyticsView.render(currentUser);
            case "8" -> testingCenterView.renderMenu(scanner);
            case "9" -> handleProfileAndSettings();
            case "10" -> {
                System.out.println("\n  Logged out successfully.");
                currentUser = null;
            }
            default -> System.out.println("\n  [ERROR] Invalid option. Please select 1-10.");
        }
    }

    private void runTechnicianShell() {
        ConsoleTheme.printAppShell(currentUser, "Technician Incident Workbench");
        System.out.println("  1. Dashboard Overview");
        System.out.println("  2. My Assigned Work Queue");
        System.out.println("  3. Inspect Ticket by ID");
        System.out.println("  4. Start Work on Ticket (IN_PROGRESS)");
        System.out.println("  5. Resolve Ticket with Notes (RESOLVED)");
        System.out.println("  6. View In-System Notifications");
        System.out.println("  7. Profile & Settings");
        System.out.println("  8. Logout");
        System.out.print("\n  Select an option (1-8): ");
        String choice = readInput();

        switch (choice) {
            case "1" -> dashboardView.render(currentUser);
            case "2" -> techWorkspaceView.renderWorkspace(currentUser);
            case "3" -> handleInspectTicketPrompt();
            case "4" -> handleStartTicketPrompt();
            case "5" -> handleResolveTicketPrompt();
            case "6" -> handleViewNotifications();
            case "7" -> handleProfileAndSettings();
            case "8" -> {
                System.out.println("\n  Logged out successfully.");
                currentUser = null;
            }
            default -> System.out.println("\n  [ERROR] Invalid option. Please select 1-8.");
        }
    }

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================

    private void handleCreateTicketPrompt() {
        ConsoleTheme.printSection("CREATE MAINTENANCE INCIDENT REPORT");
        System.out.print("  Title:       ");
        String title = readInput();
        System.out.print("  Description: ");
        String desc = readInput();
        System.out.print("  Location:    ");
        String loc = readInput();
        System.out.println("  Select Category: 1.HARDWARE 2.SOFTWARE 3.NETWORK 4.ELECTRICAL 5.FACILITY 6.OTHER");
        System.out.print("  Choice (1-6): ");
        String catInput = readInput();

        int catIdx = 0;
        try {
            catIdx = Integer.parseInt(catInput) - 1;
        } catch (NumberFormatException ignored) {
        }
        if (catIdx < 0 || catIdx >= Category.values().length) catIdx = 0;
        Category category = Category.values()[catIdx];

        try {
            Ticket ticket = ticketService.createTicket(currentUser.getId(), title, desc, category, loc, null);
            System.out.println("\n  [SUCCESS] Maintenance Ticket Submitted!");
            System.out.printf("  Ticket #%d | Priority: %s | Status: %s | Target SLA: %d Hours\n",
                    ticket.getId(), ticket.getPriority(), ticket.getStatus(), slaService.getSlaTargetDuration(ticket.getPriority()).toHours());
        } catch (ValidationException e) {
            System.out.println("\n  [FAILED] Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n  [ERROR] Ticket creation error: " + e.getMessage());
        }
    }

    private void handleInspectTicketPrompt() {
        System.out.print("\n  Enter Ticket ID to inspect: ");
        Long id = parseLongInput(readInput());
        if (id == null) {
            System.out.println("  [ERROR] Invalid ID format.");
            return;
        }
        try {
            Ticket t = ticketService.getTicketById(id);
            ticketView.renderDetailedTicketInspector(t);
        } catch (TicketNotFoundException e) {
            System.out.println("  [FAILED] " + e.getMessage());
        }
    }

    private void handleAssignTechnicianPrompt() {
        ConsoleTheme.printSection("TECHNICIAN ASSIGNMENT");
        List<Ticket> openTickets = ticketService.listAllTickets().stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN)
                .toList();

        if (openTickets.isEmpty()) {
            System.out.println("  No OPEN tickets currently awaiting assignment.");
            return;
        }

        System.out.println("  Open Incidents Awaiting Technician:");
        ticketView.renderTicketList(openTickets, "Unassigned Open Tickets");

        System.out.println("\n  Available Active Technicians:");
        List<User> techs = assignmentService.getAvailableTechnicians();
        for (User u : techs) {
            long workload = assignmentService.getTechnicianActiveWorkload(u.getId());
            System.out.printf("  - ID #%d : %-20s (%-12s) | Active Workload: %d tickets\n",
                    u.getId(), u.getFullName(), u.getUsername(), workload);
        }

        System.out.print("\n  Enter Ticket ID to assign: ");
        Long ticketId = parseLongInput(readInput());
        System.out.print("  Enter Technician ID:       ");
        Long techId = parseLongInput(readInput());

        if (ticketId == null || techId == null) {
            System.out.println("  [ERROR] Invalid ID inputs.");
            return;
        }

        try {
            Ticket assigned = assignmentService.assignTechnician(ticketId, techId, currentUser.getId());
            System.out.printf("\n  [SUCCESS] Ticket #%d assigned to %s. Status updated to %s.\n",
                    assigned.getId(), assigned.getAssignedTechnician().getFullName(), assigned.getStatus());
        } catch (Exception e) {
            System.out.println("\n  [FAILED] Assignment error: " + e.getMessage());
        }
    }

    private void handleStartTicketPrompt() {
        System.out.print("\n  Enter Ticket ID to begin work on: ");
        Long id = parseLongInput(readInput());
        if (id == null) {
            System.out.println("  [ERROR] Invalid ID.");
            return;
        }
        try {
            Ticket t = ticketService.startProgress(id, currentUser.getId());
            System.out.printf("  [SUCCESS] Ticket #%d status updated to %s.\n", t.getId(), t.getStatus());
        } catch (Exception e) {
            System.out.println("  [FAILED] " + e.getMessage());
        }
    }

    private void handleResolveTicketPrompt() {
        System.out.print("\n  Enter Ticket ID to resolve: ");
        Long id = parseLongInput(readInput());
        if (id == null) {
            System.out.println("  [ERROR] Invalid ID.");
            return;
        }
        System.out.print("  Enter Resolution Notes: ");
        String notes = readInput();

        try {
            Ticket t = ticketService.resolveTicket(id, currentUser.getId(), notes);
            System.out.printf("  [SUCCESS] Ticket #%d marked as %s.\n", t.getId(), t.getStatus());
            SlaStatus sla = slaService.calculateSlaStatus(t, null);
            System.out.printf("  SLA Compliance: %s (Window: %d Hours)\n",
                    ConsoleTheme.formatSlaStatus(sla), slaService.getSlaTargetDuration(t.getPriority()).toHours());
        } catch (Exception e) {
            System.out.println("  [FAILED] " + e.getMessage());
        }
    }

    private void handleSubmitFeedbackPrompt() {
        ConsoleTheme.printSection("USER SATISFACTION FEEDBACK");
        System.out.print("  Enter Resolved or Closed Ticket ID: ");
        Long id = parseLongInput(readInput());
        if (id == null) {
            System.out.println("  [ERROR] Invalid ID.");
            return;
        }

        System.out.print("  Rating (1 to 5 Stars): ");
        int rating = 5;
        try {
            rating = Integer.parseInt(readInput());
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Rating must be an integer.");
            return;
        }

        System.out.print("  Comments: ");
        String comments = readInput();

        try {
            Feedback f = feedbackService.submitFeedback(id, currentUser.getId(), rating, comments);
            System.out.printf("\n  [SUCCESS] Thank you! Feedback rating %d/5 recorded for Ticket #%d.\n",
                    f.getRating(), f.getTicketId());
        } catch (Exception e) {
            System.out.println("\n  [FAILED] " + e.getMessage());
        }
    }

    private void handleViewUsers() {
        ConsoleTheme.printSection("REGISTERED USER DIRECTORY");
        List<User> users = userService.listUsers();
        System.out.printf("  %-4s | %-15s | %-12s | %-22s | %-10s | %s\n",
                "ID", "USERNAME", "ROLE", "EMAIL", "STATUS", "FULL NAME");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);
        for (User u : users) {
            System.out.printf("  #%-3d | %-15s | %-12s | %-22s | %-10s | %s\n",
                    u.getId(), u.getUsername(), u.getRole(), u.getEmail(), u.getStatus(), u.getFullName());
        }
    }

    private void handleViewNotifications() {
        ConsoleTheme.printSection("IN-SYSTEM NOTIFICATIONS");
        List<Notification> notes = notificationService.getUserNotifications(currentUser.getId());
        if (notes.isEmpty()) {
            System.out.println("  No notifications.");
            return;
        }
        for (Notification n : notes) {
            System.out.printf("  * [%-10s] %s -> %s\n", n.getType(), n.getTitle(), n.getMessage());
            notificationService.markAsRead(n.getId());
        }
    }

    private void handleProfileAndSettings() {
        profileSettingsView.renderProfile(currentUser);
        profileSettingsView.renderSettings(currentUser);
    }

    // =========================================================================
    // AUTOMATED LIVE DEMONSTRATION RUNNER (--demo)
    // =========================================================================

    public void runAutomatedDemoScenario() {
        System.out.println("================================================================================");
        System.out.println("          FIXFLOW - SMART MAINTENANCE & INCIDENT MANAGEMENT SYSTEM              ");
        System.out.println("                    Automated Live Demonstration Scenario                       ");
        System.out.println("================================================================================\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Step 1: Login as User
        System.out.println(">>> STEP 1: Authenticating as standard User (john_user)...");
        User user = authService.login("john_user", DemoDataLoader.DEFAULT_PASSWORD);
        System.out.println("    [SUCCESS] Logged in as: " + user.getFullName() + " | Role: " + user.getRole() + " | Email: " + user.getEmail());

        // Step 2 & 3: Create Maintenance Ticket
        System.out.println("\n>>> STEP 2 & 3: User creates a new Maintenance Ticket...");
        Ticket createdTicket = ticketService.createTicket(
                user.getId(),
                "Main Conference Room AC Leaking Water",
                "Water dripping directly above projector table, causing electrical hazard risk.",
                Category.FACILITY,
                "Building C, Room 501",
                Priority.HIGH
        );
        System.out.println("    [SUCCESS] Ticket Created Successfully!");
        System.out.println("    - Ticket ID: #" + createdTicket.getId());
        System.out.println("    - Title: " + createdTicket.getTitle());
        System.out.println("    - Category: " + createdTicket.getCategory());
        System.out.println("    - Priority: " + createdTicket.getPriority());
        System.out.println("    - Initial Status: " + createdTicket.getStatus());
        System.out.println("    - Created At: " + createdTicket.getCreatedAt().format(dtf));

        // Step 4: Login as Administrator
        System.out.println("\n>>> STEP 4: Authenticating as Administrator (admin)...");
        User admin = authService.login("admin", DemoDataLoader.DEFAULT_PASSWORD);
        System.out.println("    [SUCCESS] Logged in as Admin: " + admin.getFullName() + " | Role: " + admin.getRole());

        // Step 5: Assign Technician
        System.out.println("\n>>> STEP 5: Administrator assigns Ticket #" + createdTicket.getId() + " to Technician (Bob)...");
        User tech = userService.getUserByUsername("tech_bob");
        Ticket assignedTicket = assignmentService.assignTechnician(createdTicket.getId(), tech.getId(), admin.getId());
        System.out.println("    [SUCCESS] Technician Assigned!");
        System.out.println("    - Ticket ID: #" + assignedTicket.getId());
        System.out.println("    - Assigned To: " + assignedTicket.getAssignedTechnician().getFullName() + " (" + assignedTicket.getAssignedTechnician().getUsername() + ")");
        System.out.println("    - New Status: " + assignedTicket.getStatus());

        // Step 6: Start Progress
        System.out.println("\n>>> STEP 6: Technician begins inspection -> Moving status to IN_PROGRESS...");
        Ticket inProgressTicket = ticketService.startProgress(assignedTicket.getId(), tech.getId());
        System.out.println("    [SUCCESS] Ticket status changed to: " + inProgressTicket.getStatus());

        // Step 7 & 8: Login as Technician and Resolve Ticket
        System.out.println("\n>>> STEP 7 & 8: Technician resolves the incident...");
        User techLogin = authService.login("tech_bob", DemoDataLoader.DEFAULT_PASSWORD);
        Ticket resolvedTicket = ticketService.resolveTicket(
                inProgressTicket.getId(),
                techLogin.getId(),
                "Cleared condensate drainage line blockage and replaced filter. Normal operation restored."
        );
        System.out.println("    [SUCCESS] Ticket Resolved!");
        System.out.println("    - Status: " + resolvedTicket.getStatus());
        System.out.println("    - Resolved At: " + resolvedTicket.getResolvedAt().format(dtf));
        System.out.println("    - Resolution Notes: " + resolvedTicket.getResolutionNotes());

        // Step 9: Calculate SLA
        System.out.println("\n>>> STEP 9: Calculating SLA Performance & Deadline...");
        SlaStatus slaStatus = slaService.calculateSlaStatus(resolvedTicket, null);
        System.out.println("    - Target SLA Window: " + slaService.getSlaTargetDuration(resolvedTicket.getPriority()).toHours() + " hours");
        System.out.println("    - Calculated SLA Status: " + slaStatus);
        System.out.println("    - SLA Met Confirmation: " + (slaStatus == SlaStatus.MET ? "YES (Within Deadline)" : "NO (Violated)"));

        // Step 10: Close Ticket
        System.out.println("\n>>> STEP 10: User reviews resolution and Closes the Ticket...");
        Ticket closedTicket = ticketService.closeTicket(resolvedTicket.getId(), user.getId());
        System.out.println("    [SUCCESS] Ticket Closed!");
        System.out.println("    - Final Status: " + closedTicket.getStatus());
        System.out.println("    - Closed At: " + closedTicket.getClosedAt().format(dtf));

        // Step 10.5: Submit Feedback
        System.out.println("\n>>> SUBMITTING USER SATISFACTION FEEDBACK...");
        Feedback feedback = feedbackService.submitFeedback(closedTicket.getId(), user.getId(), 5, "Fast response and excellent maintenance work!");
        System.out.println("    [SUCCESS] Feedback Recorded: Rating " + feedback.getRating() + "/5 stars | Comment: \"" + feedback.getComment() + "\"");

        // Step 11: Display In-System Notifications
        System.out.println("\n>>> STEP 11: Inspecting User and Technician In-System Notifications...");
        List<Notification> userNotifications = notificationService.getUserNotifications(user.getId());
        System.out.println("    Notifications for User (" + user.getUsername() + ") [Total: " + userNotifications.size() + "]:");
        for (Notification n : userNotifications) {
            System.out.println("    * [" + n.getType() + "] " + n.getTitle() + " -> " + n.getMessage());
        }

        // Step 12: Executive Analytics & Summary Report
        System.out.println("\n>>> STEP 12: Generating FixFlow System Analytics & Metrics Report...");
        SystemSummaryReport report = reportService.generateSummaryReport();
        System.out.println("    +-------------------------------------------------------------+");
        System.out.println("    |               EXECUTIVE SYSTEM SUMMARY REPORT               |");
        System.out.println("    +-------------------------------------------------------------+");
        System.out.printf("    | Total Tickets Managed:        %-29d |\n", report.totalTickets());
        System.out.printf("    | Open Tickets:                 %-29d |\n", report.openTickets());
        System.out.printf("    | In Progress Tickets:          %-29d |\n", report.inProgressTickets());
        System.out.printf("    | Resolved Tickets:             %-29d |\n", report.resolvedTickets());
        System.out.printf("    | Closed Tickets:               %-29d |\n", report.closedTickets());
        System.out.printf("    | Critical Priority Incidents:  %-29d |\n", report.criticalTickets());
        System.out.printf("    | SLA Compliance Rate:          %-26.2f%% |\n", report.slaComplianceRatePercentage());
        System.out.printf("    | Total SLA Violations:         %-29d |\n", report.slaViolations());
        System.out.println("    +-------------------------------------------------------------+");
        System.out.println("    | Breakdown by Category: " + report.ticketsByCategory());
        System.out.println("    | Breakdown by Technician: " + report.ticketsByTechnician());
        System.out.println("    +-------------------------------------------------------------+");

        System.out.println("\n================================================================================");
        System.out.println("                   DEMONSTRATION COMPLETED SUCCESSFULLY!                        ");
        System.out.println("================================================================================");
    }

    private String readInput() {
        return scanner.nextLine().trim();
    }

    private Long parseLongInput(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
