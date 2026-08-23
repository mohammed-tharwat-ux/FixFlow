package com.fixflow.ui;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;

/**
 * Visual styling, component rendering, status badges, and typography helpers for FixFlow.
 */
public final class ConsoleTheme {

    private ConsoleTheme() {
        // Prevent instantiation
    }

    public static final String DIVIDER_DOUBLE = "================================================================================";
    public static final String DIVIDER_SINGLE = "--------------------------------------------------------------------------------";
    public static final String DIVIDER_SUBTLE = "................................................................................";

    public static void printHeader(String title) {
        System.out.println("\n" + DIVIDER_DOUBLE);
        System.out.printf("  %s\n", title.toUpperCase());
        System.out.println(DIVIDER_DOUBLE);
    }

    public static void printSection(String sectionTitle) {
        System.out.println("\n" + DIVIDER_SINGLE);
        System.out.printf("  [ %s ]\n", sectionTitle);
        System.out.println(DIVIDER_SINGLE);
    }

    public static void printAppShell(User currentUser, String currentSection) {
        System.out.println("\n" + DIVIDER_DOUBLE);
        System.out.println("  FIXFLOW | Smart Maintenance & Incident Management System");
        System.out.println(DIVIDER_SINGLE);
        if (currentUser != null) {
            System.out.printf("  User: %-20s | Role: %-12s | Status: [ONLINE/ACTIVE]\n",
                    currentUser.getFullName(), currentUser.getRole());
            System.out.printf("  Active Workspace: %-25s | Email: %s\n",
                    currentSection, currentUser.getEmail());
        } else {
            System.out.println("  Session: Unauthenticated Guest");
        }
        System.out.println(DIVIDER_DOUBLE);
    }

    public static String formatTicketStatus(TicketStatus status) {
        if (status == null) return "[UNKNOWN]";
        return switch (status) {
            case OPEN -> "[OPEN]";
            case ASSIGNED -> "[ASSIGNED]";
            case IN_PROGRESS -> "[IN PROGRESS]";
            case RESOLVED -> "[RESOLVED]";
            case CLOSED -> "[CLOSED]";
            case CANCELLED -> "[CANCELLED]";
        };
    }

    public static String formatPriority(Priority priority) {
        if (priority == null) return "[NONE]";
        return switch (priority) {
            case CRITICAL -> "[CRITICAL !]";
            case HIGH -> "[HIGH]";
            case MEDIUM -> "[MEDIUM]";
            case LOW -> "[LOW]";
        };
    }

    public static String formatSlaStatus(SlaStatus status) {
        if (status == null) return "[UNKNOWN]";
        return switch (status) {
            case MET -> "[SLA MET]";
            case VIOLATED -> "[SLA VIOLATED]";
            case PENDING -> "[SLA PENDING]";
        };
    }

    public static String renderBarChart(long value, long max, int barLength) {
        if (max <= 0) max = 1;
        int filled = (int) Math.min(barLength, Math.max(0, (value * barLength) / max));
        return "[" + "#".repeat(filled) + "-".repeat(barLength - filled) + "] " + value;
    }

    public static String renderProgressBar(double percentage, int barLength) {
        int filled = (int) Math.min(barLength, Math.max(0, (percentage / 100.0) * barLength));
        return "[" + "=".repeat(filled) + " ".repeat(barLength - filled) + "] " + String.format("%.1f%%", percentage);
    }
}
