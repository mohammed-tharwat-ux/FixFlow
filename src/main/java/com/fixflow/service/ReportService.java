package com.fixflow.service;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.repository.TicketRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service generating system health reports, metrics, SLA compliance statistics, and analytics.
 */
public class ReportService {

    private final TicketRepository ticketRepository;
    private final SLAService slaService;

    public ReportService(TicketRepository ticketRepository, SLAService slaService) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository, "ticketRepository cannot be null");
        this.slaService = Objects.requireNonNull(slaService, "slaService cannot be null");
    }

    /**
     * Generates a comprehensive system summary report.
     */
    public SystemSummaryReport generateSummaryReport() {
        return generateSummaryReport(LocalDateTime.now());
    }

    /**
     * Generates summary report evaluating SLAs against a given point in time (useful for deterministic testing).
     */
    public SystemSummaryReport generateSummaryReport(LocalDateTime evaluationTime) {
        List<Ticket> all = ticketRepository.findAll();

        long total = all.size();
        long open = all.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long assigned = all.stream().filter(t -> t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = all.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = all.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = all.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();
        long cancelled = all.stream().filter(t -> t.getStatus() == TicketStatus.CANCELLED).count();
        long critical = all.stream().filter(t -> t.getPriority() == Priority.CRITICAL).count();

        long slaViolated = all.stream()
                .filter(t -> slaService.calculateSlaStatus(t, evaluationTime) == SlaStatus.VIOLATED)
                .count();

        long slaMet = all.stream()
                .filter(t -> slaService.calculateSlaStatus(t, evaluationTime) == SlaStatus.MET)
                .count();

        long completedTickets = resolved + closed;
        double complianceRate = completedTickets > 0 ? ((double) slaMet / completedTickets) * 100.0 : 100.0;

        double avgResolutionHours = calculateAverageResolutionTimeHours();

        Map<Category, Long> byCategory = getTicketsByCategoryReport();
        Map<String, Long> byTechnician = getTicketsByTechnicianReport();

        return new SystemSummaryReport(
                total,
                open,
                assigned,
                inProgress,
                resolved,
                closed,
                cancelled,
                critical,
                slaViolated,
                slaMet,
                Math.round(complianceRate * 100.0) / 100.0,
                Math.round(avgResolutionHours * 100.0) / 100.0,
                byCategory,
                byTechnician
        );
    }

    /**
     * Computes the average resolution time in hours for all resolved and closed tickets.
     */
    public double calculateAverageResolutionTimeHours() {
        List<Ticket> completed = ticketRepository.findAll().stream()
                .filter(t -> (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED)
                        && (t.getResolvedAt() != null || t.getClosedAt() != null))
                .toList();

        if (completed.isEmpty()) {
            return 0.0;
        }

        double totalHours = completed.stream()
                .map(slaService::calculateResolutionDuration)
                .mapToDouble(Duration::toMinutes)
                .map(minutes -> minutes / 60.0)
                .sum();

        return totalHours / completed.size();
    }

    /**
     * Generates a count of tickets grouped by Category.
     */
    public Map<Category, Long> getTicketsByCategoryReport() {
        Map<Category, Long> counts = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            counts.put(c, 0L);
        }
        for (Ticket t : ticketRepository.findAll()) {
            if (t.getCategory() != null) {
                counts.put(t.getCategory(), counts.get(t.getCategory()) + 1);
            }
        }
        return counts;
    }

    /**
     * Generates a count of tickets assigned per Technician.
     */
    public Map<String, Long> getTicketsByTechnicianReport() {
        Map<String, Long> counts = new HashMap<>();
        for (Ticket t : ticketRepository.findAll()) {
            String techName = t.getAssignedTechnician() != null ? t.getAssignedTechnician().getFullName() : "Unassigned";
            counts.put(techName, counts.getOrDefault(techName, 0L) + 1);
        }
        return counts;
    }
}
