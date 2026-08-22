package com.fixflow.model;

import java.util.Map;

/**
 * Data Transfer Object representing an executive system health & metrics report.
 */
public record SystemSummaryReport(
        long totalTickets,
        long openTickets,
        long assignedTickets,
        long inProgressTickets,
        long resolvedTickets,
        long closedTickets,
        long cancelledTickets,
        long criticalTickets,
        long slaViolations,
        long slaMet,
        double slaComplianceRatePercentage,
        double averageResolutionTimeHours,
        Map<Category, Long> ticketsByCategory,
        Map<String, Long> ticketsByTechnician
) {
}
