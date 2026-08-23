package com.fixflow.ui.views;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.ui.ConsoleTheme;

import java.util.List;

/**
 * Specialized workbench interface for technicians to manage assigned incidents.
 */
public class TechnicianWorkspaceView {

    private final TicketService ticketService;
    private final SLAService slaService;

    public TechnicianWorkspaceView(TicketService ticketService, SLAService slaService) {
        this.ticketService = ticketService;
        this.slaService = slaService;
    }

    public void renderWorkspace(User technician) {
        ConsoleTheme.printAppShell(technician, "Technician Incident Workbench");

        List<Ticket> assigned = ticketService.getTicketsByTechnician(technician.getId());

        List<Ticket> active = assigned.stream()
                .filter(t -> t.getStatus() == TicketStatus.ASSIGNED || t.getStatus() == TicketStatus.IN_PROGRESS)
                .toList();

        System.out.println("  Active Work Queue (" + active.size() + " ongoing assignments):");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        if (active.isEmpty()) {
            System.out.println("  No active tasks in your queue. All current assignments completed!");
            return;
        }

        System.out.printf("  %-4s | %-12s | %-12s | %-10s | %-12s | %s\n",
                "ID", "STATUS", "PRIORITY", "CATEGORY", "SLA STATUS", "TITLE & LOCATION");
        System.out.println("  " + ConsoleTheme.DIVIDER_SINGLE);

        for (Ticket t : active) {
            SlaStatus sla = slaService.calculateSlaStatus(t, null);
            System.out.printf("  #%-3d | %-12s | %-12s | %-10s | %-12s | %s (%s)\n",
                    t.getId(),
                    ConsoleTheme.formatTicketStatus(t.getStatus()),
                    ConsoleTheme.formatPriority(t.getPriority()),
                    t.getCategory(),
                    ConsoleTheme.formatSlaStatus(sla),
                    t.getTitle(),
                    t.getLocation()
            );
        }
    }
}
