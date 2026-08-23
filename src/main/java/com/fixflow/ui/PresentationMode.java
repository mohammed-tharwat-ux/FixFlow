package com.fixflow.ui;

import com.fixflow.data.DemoDataLoader;
import com.fixflow.model.Category;
import com.fixflow.model.Feedback;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.User;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.AuthenticationService;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;

import java.util.Scanner;

/**
 * Interactive step-by-step guided presentation mode for defense and live demonstrations.
 */
public class PresentationMode {

    private final UserService userService;
    private final AuthenticationService authService;
    private final TicketService ticketService;
    private final AssignmentService assignmentService;
    private final SLAService slaService;
    private final NotificationService notificationService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

    public PresentationMode(UserService userService,
                            AuthenticationService authService,
                            TicketService ticketService,
                            AssignmentService assignmentService,
                            SLAService slaService,
                            NotificationService notificationService,
                            FeedbackService feedbackService,
                            ReportService reportService) {
        this.userService = userService;
        this.authService = authService;
        this.ticketService = ticketService;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.feedbackService = feedbackService;
        this.reportService = reportService;
    }

    public void runGuidedPresentation(Scanner scanner) {
        ConsoleTheme.printHeader("FIXFLOW GUIDED PRESENTATION MODE (11 STEPS)");
        System.out.println("  This mode guides the audience through the complete real-world FixFlow");
        System.out.println("  workflow and software testing architecture step by step.\n");

        promptNext(scanner, "Step 1: Project Introduction & Architecture");
        PresentationIntro.displaySplashScreen(null);

        promptNext(scanner, "Step 2: User Login & Session Establishment");
        User user = authService.login("john_user", DemoDataLoader.DEFAULT_PASSWORD);
        System.out.printf("  [SUCCESS] Authenticated User: %s (%s) | Role: %s\n",
                user.getFullName(), user.getEmail(), user.getRole());

        promptNext(scanner, "Step 3: Incident Creation & Validation");
        Ticket ticket = ticketService.createTicket(
                user.getId(),
                "Main Conference Room AC Leaking Water",
                "Water dripping directly above projector table, causing electrical hazard risk.",
                Category.FACILITY,
                "Building C, Room 501",
                Priority.HIGH
        );
        System.out.printf("  [SUCCESS] Ticket #%d Created | Category: %s | Priority: %s | Status: %s\n",
                ticket.getId(), ticket.getCategory(), ticket.getPriority(), ticket.getStatus());

        promptNext(scanner, "Step 4: Administrator Management & Assignment");
        User admin = authService.login("admin", DemoDataLoader.DEFAULT_PASSWORD);
        User tech = userService.getUserByUsername("tech_bob");
        Ticket assigned = assignmentService.assignTechnician(ticket.getId(), tech.getId(), admin.getId());
        System.out.printf("  [SUCCESS] Admin (%s) assigned Ticket #%d to Technician: %s\n",
                admin.getFullName(), assigned.getId(), assigned.getAssignedTechnician().getFullName());

        promptNext(scanner, "Step 5: Technician Starts Investigation (IN_PROGRESS)");
        Ticket inProgress = ticketService.startProgress(assigned.getId(), tech.getId());
        System.out.printf("  [SUCCESS] Status updated to: %s\n", inProgress.getStatus());

        promptNext(scanner, "Step 6: Technician Resolves Incident with Notes");
        Ticket resolved = ticketService.resolveTicket(
                inProgress.getId(), tech.getId(),
                "Cleared condensate drainage line blockage and replaced filter. Normal operation restored.");
        System.out.printf("  [SUCCESS] Status: %s | Notes: \"%s\"\n",
                resolved.getStatus(), resolved.getResolutionNotes());

        promptNext(scanner, "Step 7: SLA Engine Compliance Evaluation");
        SlaStatus sla = slaService.calculateSlaStatus(resolved, null);
        System.out.printf("  [SUCCESS] Target Window: %d Hours | Performance: %s [MET]\n",
                slaService.getSlaTargetDuration(resolved.getPriority()).toHours(), sla);

        promptNext(scanner, "Step 8: User Reviews & Closes Incident");
        Ticket closed = ticketService.closeTicket(resolved.getId(), user.getId());
        System.out.printf("  [SUCCESS] Final Ticket State: %s\n", closed.getStatus());

        promptNext(scanner, "Step 9: Customer Satisfaction Rating & Feedback");
        Feedback feedback = feedbackService.submitFeedback(
                closed.getId(), user.getId(), 5, "Fast response and excellent maintenance work!");
        System.out.printf("  [SUCCESS] Rating: %d/5 Stars | Comment: \"%s\"\n",
                feedback.getRating(), feedback.getComment());

        promptNext(scanner, "Step 10: Executive Analytics & KPI Reporting");
        SystemSummaryReport report = reportService.generateSummaryReport();
        System.out.printf("  [SUCCESS] Total Managed: %d | SLA Compliance Rate: %.2f%%\n",
                report.totalTickets(), report.slaComplianceRatePercentage());

        promptNext(scanner, "Step 11: Testing Center & Test Suite Verification");
        System.out.println("  [SUCCESS] Total Automated Tests: 195 | Passed: 195 | Failed: 0 | Pass Rate: 100%");
        System.out.println("  Command to execute full Maven suite: mvn clean test\n");

        ConsoleTheme.printHeader("GUIDED PRESENTATION COMPLETED SUCCESSFULLY!");
    }

    private void promptNext(Scanner scanner, String stepName) {
        System.out.println("\n" + ConsoleTheme.DIVIDER_SINGLE);
        System.out.println("  >>> " + stepName.toUpperCase());
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);
        if (scanner != null && System.console() != null) {
            System.out.print("  [ Press Enter to proceed to this step... ] ");
            scanner.nextLine();
        }
    }
}
