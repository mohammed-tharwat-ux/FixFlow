package com.fixflow.service;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportService Tests")
class ReportServiceTest {

    private TicketRepository ticketRepository;
    private SLAService slaService;
    private ReportService reportService;

    private User tech1;
    private User tech2;

    @BeforeEach
    void setUp() {
        ticketRepository = new InMemoryTicketRepository();
        slaService = new SLAService();
        reportService = new ReportService(ticketRepository, slaService);

        tech1 = User.builder().id(1L).fullName("Bob Tech").username("bob").email("bob@fixflow.com").role(Role.TECHNICIAN).build();
        tech2 = User.builder().id(2L).fullName("Alice Tech").username("alice").email("alice@fixflow.com").role(Role.TECHNICIAN).build();

        LocalDateTime base = LocalDateTime.of(2026, 8, 22, 8, 0, 0);

        // Ticket 1: RESOLVED within SLA (CRITICAL, 2h target, resolved in 1h)
        ticketRepository.save(Ticket.builder()
                .title("T1")
                .description("Desc 1")
                .category(Category.HARDWARE)
                .priority(Priority.CRITICAL)
                .status(TicketStatus.RESOLVED)
                .assignedTechnician(tech1)
                .createdAt(base)
                .resolvedAt(base.plusHours(1))
                .build());

        // Ticket 2: CLOSED violating SLA (HIGH, 8h target, closed after 10h)
        ticketRepository.save(Ticket.builder()
                .title("T2")
                .description("Desc 2")
                .category(Category.NETWORK)
                .priority(Priority.HIGH)
                .status(TicketStatus.CLOSED)
                .assignedTechnician(tech2)
                .createdAt(base)
                .closedAt(base.plusHours(10))
                .build());

        // Ticket 3: OPEN (FACILITY, LOW)
        ticketRepository.save(Ticket.builder()
                .title("T3")
                .description("Desc 3")
                .category(Category.FACILITY)
                .priority(Priority.LOW)
                .status(TicketStatus.OPEN)
                .createdAt(base)
                .build());
    }

    @Test
    @DisplayName("Generate accurate summary report counts and metrics")
    void shouldGenerateSummaryReportAccurately() {
        SystemSummaryReport report = reportService.generateSummaryReport(LocalDateTime.of(2026, 8, 22, 12, 0, 0));

        assertEquals(3, report.totalTickets());
        assertEquals(1, report.openTickets());
        assertEquals(1, report.resolvedTickets());
        assertEquals(1, report.closedTickets());
        assertEquals(1, report.criticalTickets());

        assertEquals(1, report.slaMet());
        assertEquals(1, report.slaViolations());
        assertEquals(50.0, report.slaComplianceRatePercentage());

        // Average resolution time: (1h + 10h) / 2 = 5.5 hours
        assertEquals(5.5, report.averageResolutionTimeHours());

        assertEquals(1, report.ticketsByCategory().get(Category.HARDWARE));
        assertEquals(1, report.ticketsByCategory().get(Category.NETWORK));
        assertEquals(1, report.ticketsByCategory().get(Category.FACILITY));

        assertEquals(1, report.ticketsByTechnician().get("Bob Tech"));
        assertEquals(1, report.ticketsByTechnician().get("Alice Tech"));
        assertEquals(1, report.ticketsByTechnician().get("Unassigned"));
    }
}
