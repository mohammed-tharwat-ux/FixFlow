package com.fixflow.ui.views;

import com.fixflow.model.Category;
import com.fixflow.model.Feedback;
import com.fixflow.model.Notification;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.ui.ConsoleTheme;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Views for listing, filtering, searching, and inspecting detailed maintenance ticket metadata.
 */
public class TicketView {

    private final TicketService ticketService;
    private final SLAService slaService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TicketView(TicketService ticketService,
                      SLAService slaService,
                      FeedbackService feedbackService,
                      NotificationService notificationService) {
        this.ticketService = ticketService;
        this.slaService = slaService;
        this.feedbackService = feedbackService;
        this.notificationService = notificationService;
    }

    public void renderTicketList(List<Ticket> tickets, String filterDescription) {
        System.out.println("\n  --- TICKET MANAGEMENT DIRECTORY (" + tickets.size() + " records) ---");
        if (filterDescription != null && !filterDescription.isEmpty()) {
            System.out.println("  Active Filter: " + filterDescription);
        }
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        if (tickets.isEmpty()) {
            System.out.println("  No tickets found matching the specified criteria.");
            return;
        }

        System.out.printf("  %-4s | %-12s | %-12s | %-10s | %-14s | %-12s | %s\n",
                "ID", "STATUS", "PRIORITY", "CATEGORY", "TECHNICIAN", "SLA STATUS", "TITLE");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        for (Ticket t : tickets) {
            String tech = t.getAssignedTechnician() != null ? t.getAssignedTechnician().getUsername() : "Unassigned";
            SlaStatus sla = slaService.calculateSlaStatus(t, null);
            System.out.printf("  #%-3d | %-12s | %-12s | %-10s | %-14s | %-12s | %s\n",
                    t.getId(),
                    ConsoleTheme.formatTicketStatus(t.getStatus()),
                    ConsoleTheme.formatPriority(t.getPriority()),
                    t.getCategory(),
                    tech,
                    ConsoleTheme.formatSlaStatus(sla),
                    t.getTitle()
            );
        }
    }

    public void renderDetailedTicketInspector(Ticket ticket) {
        System.out.println("\n" + ConsoleTheme.DIVIDER_DOUBLE);
        System.out.printf("  TICKET INSPECTION PANEL: #%d - %s\n", ticket.getId(), ticket.getTitle());
        System.out.println(ConsoleTheme.DIVIDER_DOUBLE);

        System.out.println("\n  [ CORE DETAILS ]");
        System.out.printf("  - Ticket ID:         #%d\n", ticket.getId());
        System.out.printf("  - Title:             %s\n", ticket.getTitle());
        System.out.printf("  - Category:          %s\n", ticket.getCategory());
        System.out.printf("  - Location:          %s\n", ticket.getLocation());
        System.out.printf("  - Priority:          %s\n", ConsoleTheme.formatPriority(ticket.getPriority()));
        System.out.printf("  - Current Status:    %s\n", ConsoleTheme.formatTicketStatus(ticket.getStatus()));
        System.out.printf("  - Reported By:       %s (%s)\n",
                ticket.getReporter() != null ? ticket.getReporter().getFullName() : "N/A",
                ticket.getReporter() != null ? ticket.getReporter().getEmail() : "N/A");
        System.out.printf("  - Assigned Tech:     %s\n",
                ticket.getAssignedTechnician() != null ? ticket.getAssignedTechnician().getFullName() + " (" + ticket.getAssignedTechnician().getUsername() + ")" : "Unassigned");
        System.out.printf("  - Description:       %s\n", ticket.getDescription());

        // SLA Performance Panel
        System.out.println("\n  [ SLA METRICS & PERFORMANCE ]");
        Duration targetSla = slaService.getSlaTargetDuration(ticket.getPriority());
        SlaStatus slaStatus = slaService.calculateSlaStatus(ticket, null);
        Duration remaining = slaService.getRemainingTime(ticket, null);

        System.out.printf("  - Target SLA Window: %d hours\n", targetSla.toHours());
        System.out.printf("  - SLA Compliance:    %s\n", ConsoleTheme.formatSlaStatus(slaStatus));
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            Duration resDuration = slaService.calculateResolutionDuration(ticket);
            System.out.printf("  - Total Resolution:  %d hrs %d mins\n", resDuration.toHours(), resDuration.toMinutesPart());
        } else {
            System.out.printf("  - Remaining Time:    %s\n",
                    remaining.isNegative() ? "OVERDUE by " + Math.abs(remaining.toHours()) + " hrs" : remaining.toHours() + " hrs " + remaining.toMinutesPart() + " mins");
        }

        // Timeline Lifecycle
        System.out.println("\n  [ INCIDENT TIMELINE ]");
        System.out.printf("  [+] 1. CREATED:      %s\n", ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(dtf) : "N/A");
        System.out.printf("  [+] 2. ASSIGNED:     %s\n", (ticket.getAssignedTechnician() != null) ? "Yes (Assigned to " + ticket.getAssignedTechnician().getUsername() + ")" : "Pending");
        System.out.printf("  [+] 3. IN PROGRESS:  %s\n", (ticket.getStatus() == TicketStatus.IN_PROGRESS || ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) ? "Work Commenced" : "Pending");
        System.out.printf("  [+] 4. RESOLVED:     %s\n", ticket.getResolvedAt() != null ? ticket.getResolvedAt().format(dtf) : "Not yet resolved");
        System.out.printf("  [+] 5. CLOSED:       %s\n", ticket.getClosedAt() != null ? ticket.getClosedAt().format(dtf) : "Active / Open");

        if (ticket.getResolutionNotes() != null && !ticket.getResolutionNotes().isEmpty()) {
            System.out.println("\n  [ RESOLUTION NOTES ]");
            System.out.println("  \"" + ticket.getResolutionNotes() + "\"");
        }

        // Feedback
        Optional<Feedback> feedback = feedbackService.getFeedbackForTicket(ticket.getId());
        if (feedback.isPresent()) {
            System.out.println("\n  [ USER SATISFACTION FEEDBACK ]");
            System.out.printf("  - Rating:  %d / 5 Stars [ %s ]\n", feedback.get().getRating(), "*".repeat(feedback.get().getRating()));
            System.out.printf("  - Comment: \"%s\"\n", feedback.get().getComment());
        }

        System.out.println("\n" + ConsoleTheme.DIVIDER_DOUBLE);
    }
}
