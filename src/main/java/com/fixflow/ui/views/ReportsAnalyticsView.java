package com.fixflow.ui.views;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.User;
import com.fixflow.service.ReportService;
import com.fixflow.service.TicketService;
import com.fixflow.ui.ConsoleTheme;

import java.util.Map;

/**
 * Visual analytics and executive management reports with ASCII data visualizations.
 */
public class ReportsAnalyticsView {

    private final ReportService reportService;
    private final TicketService ticketService;

    public ReportsAnalyticsView(ReportService reportService, TicketService ticketService) {
        this.reportService = reportService;
        this.ticketService = ticketService;
    }

    public void render(User currentUser) {
        ConsoleTheme.printAppShell(currentUser, "Executive Analytics & Operations Reports");

        SystemSummaryReport report = reportService.generateSummaryReport();

        System.out.println("  +--- EXECUTIVE PERFORMANCE METRICS ------------------------------------------+");
        System.out.printf("  | Total Managed Tickets:     %-8d | Overall SLA Compliance: %-15s |\n",
                report.totalTickets(), String.format("%.2f%%", report.slaComplianceRatePercentage()));
        System.out.printf("  | Average Resolution Time:   %-6.2f hrs | Total SLA Violations:   %-15d |\n",
                report.averageResolutionTimeHours(), report.slaViolations());
        System.out.println("  +----------------------------------------------------------------------------+");

        System.out.println("\n  [+] SLA COMPLIANCE PERFORMANCE:");
        System.out.println("  " + ConsoleTheme.renderProgressBar(report.slaComplianceRatePercentage(), 30));

        System.out.println("\n  [+] INCIDENT DISTRIBUTION BY STATUS:");
        long maxStatus = Math.max(1, report.totalTickets());
        System.out.printf("  - OPEN        : %s\n", ConsoleTheme.renderBarChart(report.openTickets(), maxStatus, 20));
        System.out.printf("  - ASSIGNED    : %s\n", ConsoleTheme.renderBarChart(report.assignedTickets(), maxStatus, 20));
        System.out.printf("  - IN PROGRESS : %s\n", ConsoleTheme.renderBarChart(report.inProgressTickets(), maxStatus, 20));
        System.out.printf("  - RESOLVED    : %s\n", ConsoleTheme.renderBarChart(report.resolvedTickets(), maxStatus, 20));
        System.out.printf("  - CLOSED      : %s\n", ConsoleTheme.renderBarChart(report.closedTickets(), maxStatus, 20));
        System.out.printf("  - CANCELLED   : %s\n", ConsoleTheme.renderBarChart(report.cancelledTickets(), maxStatus, 20));

        System.out.println("\n  [+] INCIDENT DISTRIBUTION BY CATEGORY:");
        for (Category c : Category.values()) {
            long count = report.ticketsByCategory().getOrDefault(c, 0L);
            System.out.printf("  - %-12s: %s\n", c, ConsoleTheme.renderBarChart(count, maxStatus, 20));
        }

        System.out.println("\n  [+] TECHNICIAN LOAD & ASSIGNMENTS:");
        for (Map.Entry<String, Long> entry : report.ticketsByTechnician().entrySet()) {
            System.out.printf("  - %-20s: %s\n", entry.getKey(), ConsoleTheme.renderBarChart(entry.getValue(), maxStatus, 20));
        }

        System.out.println("\n" + ConsoleTheme.DIVIDER_DOUBLE);
    }
}
