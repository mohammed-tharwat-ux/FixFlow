package com.fixflow.ui;

import com.fixflow.data.DemoDataLoader;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application Runner and Interactive Console UI for FixFlow.
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

        // Preload demo seed data
        DemoDataLoader.loadDemoData(userService, ticketService, assignmentService, slaService);
    }

    public static void main(String[] args) {
        FixFlowApp app = new FixFlowApp();

        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            app.runAutomatedDemoScenario();
            return;
        }

        // Check if console is available for interactive mode
        if (System.console() == null && args.length == 0) {
            app.runAutomatedDemoScenario();
        } else {
            app.runInteractiveLoop();
        }
    }

    /**
     * Executes the comprehensive 15-step demonstration scenario as requested in specifications.
     */
    public void runAutomatedDemoScenario() {
        System.out.println("================================================================================");
        System.out.println("          FIXFLOW — SMART MAINTENANCE & INCIDENT MANAGEMENT SYSTEM              ");
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

    public void runInteractiveLoop() {
        System.out.println("\n=== Welcome to FixFlow Interactive Console ===");
        boolean running = true;
        while (running) {
            if (currentUser == null) {
                System.out.println("\n1. Login");
                System.out.println("2. Register New User");
                System.out.println("3. Run Automated 15-Step Demo Scenario");
                System.out.println("4. Exit");
                System.out.print("Select an option: ");
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> handleLogin();
                    case "2" -> handleRegister();
                    case "3" -> runAutomatedDemoScenario();
                    case "4" -> running = false;
                    default -> System.out.println("Invalid selection.");
                }
            } else {
                displayUserMenu();
            }
        }
    }

    private void displayUserMenu() {
        System.out.println("\nLogged in as: " + currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        System.out.println("1. View All Tickets");
        System.out.println("2. Create Maintenance Ticket");
        System.out.println("3. View Notifications");
        System.out.println("4. View System Reports");
        System.out.println("5. Logout");
        System.out.print("Select an option: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> listTickets();
            case "2" -> createTicketPrompt();
            case "3" -> viewNotificationsPrompt();
            case "4" -> viewReportsPrompt();
            case "5" -> currentUser = null;
            default -> System.out.println("Invalid selection.");
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String u = scanner.nextLine();
        System.out.print("Enter password: ");
        String p = scanner.nextLine();
        try {
            currentUser = authService.login(u, p);
            System.out.println("Welcome, " + currentUser.getFullName() + "!");
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void handleRegister() {
        try {
            System.out.print("Full Name: ");
            String name = scanner.nextLine();
            System.out.print("Username: ");
            String uname = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();
            User reg = userService.registerUser(name, uname, email, pass, Role.USER);
            System.out.println("User registered successfully! ID: " + reg.getId());
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }
    }

    private void listTickets() {
        List<Ticket> tickets = ticketService.listAllTickets();
        System.out.println("\n--- All Maintenance Tickets (" + tickets.size() + ") ---");
        for (Ticket t : tickets) {
            System.out.printf("#%-3d | %-12s | %-8s | %-10s | %s\n",
                    t.getId(), t.getStatus(), t.getPriority(), t.getCategory(), t.getTitle());
        }
    }

    private void createTicketPrompt() {
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Description: ");
            String desc = scanner.nextLine();
            System.out.print("Location: ");
            String loc = scanner.nextLine();
            System.out.println("Categories: 1.HARDWARE 2.SOFTWARE 3.NETWORK 4.ELECTRICAL 5.FACILITY 6.OTHER");
            System.out.print("Choice: ");
            int catChoice = Integer.parseInt(scanner.nextLine().trim());
            Category cat = Category.values()[Math.max(0, Math.min(5, catChoice - 1))];

            Ticket t = ticketService.createTicket(currentUser.getId(), title, desc, cat, loc, null);
            System.out.println("Ticket created with ID #" + t.getId() + " (Priority evaluated as " + t.getPriority() + ")");
        } catch (Exception e) {
            System.out.println("Error creating ticket: " + e.getMessage());
        }
    }

    private void viewNotificationsPrompt() {
        List<Notification> notes = notificationService.getUserNotifications(currentUser.getId());
        System.out.println("\n--- Notifications (" + notes.size() + ") ---");
        for (Notification n : notes) {
            System.out.println("[" + (n.isRead() ? "READ" : "NEW") + "] " + n.getTitle() + ": " + n.getMessage());
        }
    }

    private void viewReportsPrompt() {
        SystemSummaryReport report = reportService.generateSummaryReport();
        System.out.println("\n--- System Summary Report ---");
        System.out.println("Total Tickets: " + report.totalTickets());
        System.out.println("Resolved/Closed: " + (report.resolvedTickets() + report.closedTickets()));
        System.out.println("SLA Compliance Rate: " + report.slaComplianceRatePercentage() + "%");
    }
}
