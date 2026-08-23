package com.fixflow.ui.views;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import com.fixflow.ui.ConsoleTheme;

import java.util.List;

/**
 * Renders role-specific executive and operational dashboards.
 */
public class DashboardView {

    private final TicketService ticketService;
    private final UserService userService;
    private final AssignmentService assignmentService;
    private final ReportService reportService;
    private final SLAService slaService;

    public DashboardView(TicketService ticketService,
                         UserService userService,
                         AssignmentService assignmentService,
                         ReportService reportService,
                         SLAService slaService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.assignmentService = assignmentService;
        this.reportService = reportService;
        this.slaService = slaService;
    }

    public void render(User currentUser) {
        ConsoleTheme.printAppShell(currentUser, "Operations Dashboard");

        System.out.printf("  Welcome, %s (%s)\n", currentUser.getFullName(), currentUser.getRole());
        System.out.println(ConsoleTheme.DIVIDER_SUBTLE);

        if (currentUser.getRole() == Role.ADMIN) {
            renderAdminDashboard();
        } else if (currentUser.getRole() == Role.TECHNICIAN) {
            renderTechnicianDashboard(currentUser);
        } else {
            renderUserDashboard(currentUser);
        }
    }

    private void renderAdminDashboard() {
        SystemSummaryReport report = reportService.generateSummaryReport();

        System.out.println("\n  +--- SYSTEM OPERATIONAL KPIS ------------------------------------------------+");
        System.out.printf("  | Total Volume:   %-8d | Open / New:    %-8d | Assigned:       %-8d |\n",
                report.totalTickets(), report.openTickets(), report.assignedTickets());
        System.out.printf("  | In Progress:    %-8d | Resolved:      %-8d | Closed:         %-8d |\n",
                report.inProgressTickets(), report.resolvedTickets(), report.closedTickets());
        System.out.printf("  | Critical Count: %-8d | SLA Violations:%-8d | Compliance Rate:%-7.1f%% |\n",
                report.criticalTickets(), report.slaViolations(), report.slaComplianceRatePercentage());
        System.out.println("  +----------------------------------------------------------------------------+");

        System.out.println("\n  SLA Overall Performance: " + ConsoleTheme.renderProgressBar(report.slaComplianceRatePercentage(), 25));

        System.out.println("\n  Active Technician Workload Distribution:");
        List<User> techs = assignmentService.getAvailableTechnicians();
        for (User t : techs) {
            long load = assignmentService.getTechnicianActiveWorkload(t.getId());
            System.out.printf("  - %-20s : %s\n", t.getFullName(), ConsoleTheme.renderBarChart(load, 5, 15));
        }

        System.out.println("\n  Incidents by Category Breakdown:");
        for (Category c : Category.values()) {
            long count = report.ticketsByCategory().getOrDefault(c, 0L);
            System.out.printf("  - %-15s : %s\n", c, ConsoleTheme.renderBarChart(count, 10, 15));
        }
    }

    private void renderTechnicianDashboard(User technician) {
        List<Ticket> myTickets = ticketService.getTicketsByTechnician(technician.getId());
        long pending = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED).count();
        long critical = myTickets.stream().filter(t -> t.getPriority() == Priority.CRITICAL && t.getStatus() != TicketStatus.CLOSED).count();

        System.out.println("\n  +--- MY WORK BENCHMARK ------------------------------------------------------+");
        System.out.printf("  | Total Assigned: %-8d | Pending Action: %-8d | In Progress:     %-8d |\n",
                myTickets.size(), pending, inProgress);
        System.out.printf("  | Completed:      %-8d | Critical Alert: %-8d | Active Workload: %-8d |\n",
                resolved, critical, (pending + inProgress));
        System.out.println("  +----------------------------------------------------------------------------+");

        System.out.println("\n  Active Urgent & Critical Tasks:");
        List<Ticket> urgent = myTickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.RESOLVED)
                .limit(3)
                .toList();

        if (urgent.isEmpty()) {
            System.out.println("  No urgent pending tasks. All clear!");
        } else {
            for (Ticket t : urgent) {
                System.out.printf("  * #%-3d | %-12s | %-12s | %s (%s)\n",
                        t.getId(), ConsoleTheme.formatPriority(t.getPriority()), ConsoleTheme.formatTicketStatus(t.getStatus()), t.getTitle(), t.getLocation());
            }
        }
    }

    private void renderUserDashboard(User user) {
        List<Ticket> myTickets = ticketService.getTicketsByReporter(user.getId());
        long open = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        System.out.println("\n  +--- MY INCIDENT REPORTS ----------------------------------------------------+");
        System.out.printf("  | Total Reported: %-8d | Awaiting Tech:  %-8d | In Progress:     %-8d |\n",
                myTickets.size(), open, inProgress);
        System.out.printf("  | Resolved (New): %-8d | Closed / Done:  %-8d | Pending Review:  %-8d |\n",
                resolved, closed, resolved);
        System.out.println("  +----------------------------------------------------------------------------+");

        System.out.println("\n  Recent Ticket Statuses:");
        if (myTickets.isEmpty()) {
            System.out.println("  You have not submitted any maintenance requests yet.");
        } else {
            myTickets.stream().limit(4).forEach(t -> {
                System.out.printf("  * #%-3d | %-12s | %-10s | %s\n",
                        t.getId(), ConsoleTheme.formatTicketStatus(t.getStatus()), t.getCategory(), t.getTitle());
            });
        }
    }
}
