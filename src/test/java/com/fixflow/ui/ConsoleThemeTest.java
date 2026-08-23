package com.fixflow.ui;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsoleTheme Formatting & Badge Tests")
class ConsoleThemeTest {

    @Test
    @DisplayName("Verify ticket status badge formatting")
    void shouldFormatTicketStatusBadges() {
        assertTrue(ConsoleTheme.formatTicketStatus(TicketStatus.OPEN).contains("[OPEN]"));
        assertTrue(ConsoleTheme.formatTicketStatus(TicketStatus.IN_PROGRESS).contains("[IN PROGRESS]"));
        assertTrue(ConsoleTheme.formatTicketStatus(TicketStatus.RESOLVED).contains("[RESOLVED]"));
        assertTrue(ConsoleTheme.formatTicketStatus(TicketStatus.CLOSED).contains("[CLOSED]"));
        assertTrue(ConsoleTheme.formatTicketStatus(null).contains("[UNKNOWN]"));
    }

    @Test
    @DisplayName("Verify priority badge formatting")
    void shouldFormatPriorityBadges() {
        assertTrue(ConsoleTheme.formatPriority(Priority.CRITICAL).contains("[CRITICAL !]"));
        assertTrue(ConsoleTheme.formatPriority(Priority.HIGH).contains("[HIGH]"));
        assertTrue(ConsoleTheme.formatPriority(Priority.MEDIUM).contains("[MEDIUM]"));
        assertTrue(ConsoleTheme.formatPriority(Priority.LOW).contains("[LOW]"));
        assertTrue(ConsoleTheme.formatPriority(null).contains("[NONE]"));
    }

    @Test
    @DisplayName("Verify SLA status badge formatting")
    void shouldFormatSlaStatusBadges() {
        assertTrue(ConsoleTheme.formatSlaStatus(SlaStatus.MET).contains("[SLA MET]"));
        assertTrue(ConsoleTheme.formatSlaStatus(SlaStatus.VIOLATED).contains("[SLA VIOLATED]"));
        assertTrue(ConsoleTheme.formatSlaStatus(SlaStatus.PENDING).contains("[SLA PENDING]"));
        assertTrue(ConsoleTheme.formatSlaStatus(null).contains("[UNKNOWN]"));
    }

    @Test
    @DisplayName("Verify progress bar and bar chart calculations")
    void shouldRenderBarsAccurately() {
        String bar = ConsoleTheme.renderBarChart(5, 10, 10);
        assertTrue(bar.contains("#####"));
        assertTrue(bar.contains("-----"));
        assertTrue(bar.contains("5"));

        String progress = ConsoleTheme.renderProgressBar(50.0, 10);
        assertTrue(progress.contains("====="));
        assertTrue(progress.contains("50.0%"));
    }
}
