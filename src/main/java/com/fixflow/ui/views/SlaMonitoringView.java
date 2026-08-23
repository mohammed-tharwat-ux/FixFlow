package com.fixflow.ui.views;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.ui.ConsoleTheme;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Operations view for tracking SLA compliance, deadlines, and overdue incidents.
 */
public class SlaMonitoringView {

    private final TicketService ticketService;
    private final SLAService slaService;

    public SlaMonitoringView(TicketService ticketService, SLAService slaService) {
        this.ticketService = ticketService;
        this.slaService = slaService;
    }

    public void render(User currentUser) {
        ConsoleTheme.printAppShell(currentUser, "SLA Compliance & Real-time Monitoring");

        System.out.println("  SLA Target Windows Policy:");
        System.out.println("  * CRITICAL : 2 Hours (120 Mins)  - Emergency / Outage / Hazard");
        System.out.println("  * HIGH     : 8 Hours (480 Mins)  - Major system failure / Electrical / Server");
        System.out.println("  * MEDIUM   : 24 Hours (1 Day)    - Hardware / Network routine maintenance");
        System.out.println("  * LOW      : 72 Hours (3 Days)   - General facility / Minor service requests");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        List<Ticket> allTickets = ticketService.listAllTickets();
        long met = allTickets.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.MET).count();
        long violated = allTickets.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.VIOLATED).count();
        long pending = allTickets.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.PENDING).count();

        System.out.printf("  SLA SUMMARY:  Total: %d | [SLA MET]: %d | [SLA VIOLATED]: %d | [IN PROGRESS / PENDING]: %d\n",
                allTickets.size(), met, violated, pending);
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        System.out.printf("  %-4s | %-12s | %-12s | %-12s | %-14s | %-15s | %s\n",
                "ID", "PRIORITY", "STATUS", "SLA TARGET", "REMAINING TIME", "SLA MET?", "TITLE");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        for (Ticket t : allTickets) {
            SlaStatus status = slaService.calculateSlaStatus(t, null);
            Duration target = slaService.getSlaTargetDuration(t.getPriority());
            Duration remaining = slaService.getRemainingTime(t, null);

            String remStr;
            if (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) {
                remStr = "Finished";
            } else if (remaining.isNegative()) {
                remStr = "OVERDUE (" + Math.abs(remaining.toHours()) + "h)";
            } else {
                remStr = remaining.toHours() + "h " + remaining.toMinutesPart() + "m";
            }

            System.out.printf("  #%-3d | %-12s | %-12s | %-12s | %-14s | %-15s | %s\n",
                    t.getId(),
                    ConsoleTheme.formatPriority(t.getPriority()),
                    ConsoleTheme.formatTicketStatus(t.getStatus()),
                    target.toHours() + " Hours",
                    remStr,
                    ConsoleTheme.formatSlaStatus(status),
                    t.getTitle()
            );
        }
    }
}
