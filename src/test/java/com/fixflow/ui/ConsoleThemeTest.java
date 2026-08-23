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
        assertEquals("[OPEN]", ConsoleTheme.formatTicketStatus(TicketStatus.OPEN));
        assertEquals("[IN PROGRESS]", ConsoleTheme.formatTicketStatus(TicketStatus.IN_PROGRESS));
        assertEquals("[RESOLVED]", ConsoleTheme.formatTicketStatus(TicketStatus.RESOLVED));
        assertEquals("[CLOSED]", ConsoleTheme.formatTicketStatus(TicketStatus.CLOSED));
        assertEquals("[UNKNOWN]", ConsoleTheme.formatTicketStatus(null));
    }

    @Test
    @DisplayName("Verify priority badge formatting")
    void shouldFormatPriorityBadges() {
        assertEquals("[CRITICAL !]", ConsoleTheme.formatPriority(Priority.CRITICAL));
        assertEquals("[HIGH]", ConsoleTheme.formatPriority(Priority.HIGH));
        assertEquals("[MEDIUM]", ConsoleTheme.formatPriority(Priority.MEDIUM));
        assertEquals("[LOW]", ConsoleTheme.formatPriority(Priority.LOW));
        assertEquals("[NONE]", ConsoleTheme.formatPriority(null));
    }

    @Test
    @DisplayName("Verify SLA status badge formatting")
    void shouldFormatSlaStatusBadges() {
        assertEquals("[SLA MET]", ConsoleTheme.formatSlaStatus(SlaStatus.MET));
        assertEquals("[SLA VIOLATED]", ConsoleTheme.formatSlaStatus(SlaStatus.VIOLATED));
        assertEquals("[SLA PENDING]", ConsoleTheme.formatSlaStatus(SlaStatus.PENDING));
        assertEquals("[UNKNOWN]", ConsoleTheme.formatSlaStatus(null));
    }

    @Test
    @DisplayName("Verify progress bar and bar chart calculations")
    void shouldRenderBarsAccurately() {
        String bar = ConsoleTheme.renderBarChart(5, 10, 10);
        assertTrue(bar.contains("#####-----"));
        assertTrue(bar.contains("5"));

        String progress = ConsoleTheme.renderProgressBar(50.0, 10);
        assertTrue(progress.contains("====="));
        assertTrue(progress.contains("50.0%"));
    }
}
