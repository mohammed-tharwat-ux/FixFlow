package com.fixflow.service;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SLAService Unit & Boundary Tests")
class SLAServiceTest {

    private SLAService slaService;

    @BeforeEach
    void setUp() {
        slaService = new SLAService();
    }

    @ParameterizedTest(name = "Priority {0} has target window {1} hours")
    @CsvSource({
            "CRITICAL, 2",
            "HIGH, 8",
            "MEDIUM, 24",
            "LOW, 72"
    })
    @DisplayName("Verify target SLA duration windows for all priorities")
    void shouldReturnCorrectSlaTargets(Priority priority, long expectedHours) {
        Duration target = slaService.getSlaTargetDuration(priority);
        assertEquals(Duration.ofHours(expectedHours), target);
    }

    @Nested
    @DisplayName("SLA Boundary Value Analysis for HIGH Priority (8 Hours)")
    class HighPriorityBoundaryTests {

        private LocalDateTime createdAt;
        private LocalDateTime deadline;

        @BeforeEach
        void initTimes() {
            createdAt = LocalDateTime.of(2026, 8, 22, 8, 0, 0);
            deadline = createdAt.plusHours(8); // 16:00:00
        }

        @Test
        @DisplayName("Boundary: Resolved at 7 hours 59 minutes (Before deadline) -> MET")
        void shouldMarkSlaAsMetBeforeDeadline() {
            LocalDateTime resolvedAt = createdAt.plusHours(7).plusMinutes(59);

            Ticket ticket = Ticket.builder()
                    .id(1L)
                    .priority(Priority.HIGH)
                    .status(TicketStatus.RESOLVED)
                    .createdAt(createdAt)
                    .resolvedAt(resolvedAt)
                    .build();

            SlaStatus status = slaService.calculateSlaStatus(ticket, null);
            assertEquals(SlaStatus.MET, status);
            assertFalse(slaService.isSlaViolated(ticket, null));
        }

        @Test
        @DisplayName("Boundary: Resolved at exactly 8 hours 00 minutes -> MET")
        void shouldMarkSlaAsMetAtExactDeadline() {
            LocalDateTime resolvedAt = createdAt.plusHours(8);

            Ticket ticket = Ticket.builder()
                    .id(1L)
                    .priority(Priority.HIGH)
                    .status(TicketStatus.RESOLVED)
                    .createdAt(createdAt)
                    .resolvedAt(resolvedAt)
                    .build();

            SlaStatus status = slaService.calculateSlaStatus(ticket, null);
            assertEquals(SlaStatus.MET, status);
            assertFalse(slaService.isSlaViolated(ticket, null));
        }

        @Test
        @DisplayName("Boundary: Resolved at 8 hours 01 minutes (Past deadline) -> VIOLATED")
        void shouldMarkSlaAsViolatedAfterDeadline() {
            LocalDateTime resolvedAt = createdAt.plusHours(8).plusMinutes(1);

            Ticket ticket = Ticket.builder()
                    .id(1L)
                    .priority(Priority.HIGH)
                    .status(TicketStatus.RESOLVED)
                    .createdAt(createdAt)
                    .resolvedAt(resolvedAt)
                    .build();

            SlaStatus status = slaService.calculateSlaStatus(ticket, null);
            assertEquals(SlaStatus.VIOLATED, status);
            assertTrue(slaService.isSlaViolated(ticket, null));
        }
    }

    @Nested
    @DisplayName("Active Tickets SLA Tracking")
    class ActiveTicketsSlaTests {

        @Test
        @DisplayName("Active ticket within deadline is PENDING")
        void shouldReturnPendingForActiveTicketWithinDeadline() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 10, 0, 0);
            LocalDateTime evalTime = LocalDateTime.of(2026, 8, 22, 11, 0, 0); // 1 hour elapsed out of 2h

            Ticket ticket = Ticket.builder()
                    .id(2L)
                    .priority(Priority.CRITICAL)
                    .status(TicketStatus.IN_PROGRESS)
                    .createdAt(createdAt)
                    .build();

            assertEquals(SlaStatus.PENDING, slaService.calculateSlaStatus(ticket, evalTime));
            assertEquals(Duration.ofHours(1), slaService.getRemainingTime(ticket, evalTime));
        }

        @Test
        @DisplayName("Active ticket exceeding deadline is VIOLATED")
        void shouldReturnViolatedForActiveTicketExceedingDeadline() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 22, 10, 0, 0);
            LocalDateTime evalTime = LocalDateTime.of(2026, 8, 22, 13, 0, 0); // 3 hours elapsed out of 2h

            Ticket ticket = Ticket.builder()
                    .id(2L)
                    .priority(Priority.CRITICAL)
                    .status(TicketStatus.IN_PROGRESS)
                    .createdAt(createdAt)
                    .build();

            assertEquals(SlaStatus.VIOLATED, slaService.calculateSlaStatus(ticket, evalTime));
            assertTrue(slaService.getRemainingTime(ticket, evalTime).isNegative());
        }

        @Test
        @DisplayName("Cancelled ticket returns PENDING status")
        void shouldReturnPendingForCancelledTicket() {
            Ticket ticket = Ticket.builder()
                    .id(3L)
                    .priority(Priority.CRITICAL)
                    .status(TicketStatus.CANCELLED)
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build();

            assertEquals(SlaStatus.PENDING, slaService.calculateSlaStatus(ticket, LocalDateTime.now()));
        }
    }
}
