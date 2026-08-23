package com.fixflow.ui;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;

/**
 * Enterprise Visual Identity, ANSI Color System, Status Badges,
 * and Box-Drawing Components for FixFlow.
 */
public final class ConsoleTheme {

    private ConsoleTheme() {
        // Prevent instantiation
    }

    // --- ANSI Color Codes (Deep Navy, Cyan, Emerald, Amber, Red, White, Reset) ---
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    public static final String NAVY_BLUE = "\u001B[38;5;31m";
    public static final String BRIGHT_CYAN = "\u001B[38;5;51m";
    public static final String EMERALD_GREEN = "\u001B[38;5;42m";
    public static final String AMBER_YELLOW = "\u001B[38;5;214m";
    public static final String CORAL_RED = "\u001B[38;5;196m";
    public static final String SLATE_GRAY = "\u001B[38;5;244m";
    public static final String PURE_WHITE = "\u001B[38;5;231m";
    public static final String BG_NAVY = "\u001B[48;5;24m";

    // --- Box Drawing & Borders ---
    public static final String DIVIDER_DOUBLE = "================================================================================";
    public static final String DIVIDER_SINGLE = "--------------------------------------------------------------------------------";
    public static final String DIVIDER_SUBTLE = "................................................................................";

    public static void printBrandLogo() {
        System.out.println(NAVY_BLUE + DIVIDER_SINGLE + RESET);
        System.out.println("  " + BOLD + BRIGHT_CYAN + "FIXFLOW" + RESET + " — " + PURE_WHITE + BOLD + "Smart Maintenance & Incident Management System" + RESET);
        System.out.println("  " + SLATE_GRAY + "Enterprise Operations Control & Automated SLA Surveillance" + RESET);
        System.out.println(NAVY_BLUE + DIVIDER_SINGLE + RESET);
    }

    public static void printHeader(String title) {
        System.out.println("\n" + NAVY_BLUE + DIVIDER_DOUBLE + RESET);
        System.out.printf("  %s%s%s%s\n", BOLD, PURE_WHITE, title.toUpperCase(), RESET);
        System.out.println(NAVY_BLUE + DIVIDER_DOUBLE + RESET);
    }

    public static void printSection(String sectionTitle) {
        System.out.println("\n" + SLATE_GRAY + DIVIDER_SINGLE + RESET);
        System.out.printf("  %s%s[ %s ]%s\n", BOLD, BRIGHT_CYAN, sectionTitle, RESET);
        System.out.println(SLATE_GRAY + DIVIDER_SINGLE + RESET);
    }

    public static void printAppShell(User currentUser, String currentSection) {
        System.out.println("\n" + NAVY_BLUE + DIVIDER_DOUBLE + RESET);
        System.out.printf("  %s%sFIXFLOW%s | %sSmart Maintenance & Incident Management Platform%s\n",
                BOLD, BRIGHT_CYAN, RESET, SLATE_GRAY, RESET);
        System.out.println(SLATE_GRAY + DIVIDER_SINGLE + RESET);
        if (currentUser != null) {
            System.out.printf("  User: %s%s%-18s%s | Role: %s%-10s%s | Status: %s[ACTIVE]%s\n",
                    BOLD, PURE_WHITE, currentUser.getFullName(), RESET,
                    BRIGHT_CYAN, currentUser.getRole(), RESET,
                    EMERALD_GREEN, RESET);
            System.out.printf("  Workspace: %s%-20s%s | Email: %s%s%s\n",
                    BOLD, currentSection, RESET,
                    SLATE_GRAY, currentUser.getEmail(), RESET);
        } else {
            System.out.println("  Session: Unauthenticated Guest");
        }
        System.out.println(NAVY_BLUE + DIVIDER_DOUBLE + RESET);
    }

    public static String formatTicketStatus(TicketStatus status) {
        if (status == null) return SLATE_GRAY + "[UNKNOWN]" + RESET;
        return switch (status) {
            case OPEN -> BRIGHT_CYAN + "[OPEN]" + RESET;
            case ASSIGNED -> NAVY_BLUE + "[ASSIGNED]" + RESET;
            case IN_PROGRESS -> AMBER_YELLOW + "[IN PROGRESS]" + RESET;
            case RESOLVED -> EMERALD_GREEN + "[RESOLVED]" + RESET;
            case CLOSED -> SLATE_GRAY + "[CLOSED]" + RESET;
            case CANCELLED -> CORAL_RED + "[CANCELLED]" + RESET;
        };
    }

    public static String formatPriority(Priority priority) {
        if (priority == null) return SLATE_GRAY + "[NONE]" + RESET;
        return switch (priority) {
            case CRITICAL -> CORAL_RED + BOLD + "[CRITICAL !]" + RESET;
            case HIGH -> AMBER_YELLOW + "[HIGH]" + RESET;
            case MEDIUM -> BRIGHT_CYAN + "[MEDIUM]" + RESET;
            case LOW -> SLATE_GRAY + "[LOW]" + RESET;
        };
    }

    public static String formatSlaStatus(SlaStatus status) {
        if (status == null) return SLATE_GRAY + "[UNKNOWN]" + RESET;
        return switch (status) {
            case MET -> EMERALD_GREEN + BOLD + "[SLA MET]" + RESET;
            case VIOLATED -> CORAL_RED + BOLD + "[SLA VIOLATED]" + RESET;
            case PENDING -> BRIGHT_CYAN + "[SLA PENDING]" + RESET;
        };
    }

    public static String renderBarChart(long value, long max, int barLength) {
        if (max <= 0) max = 1;
        int filled = (int) Math.min(barLength, Math.max(0, (value * barLength) / max));
        return BRIGHT_CYAN + "[" + "#".repeat(filled) + SLATE_GRAY + "-".repeat(barLength - filled) + BRIGHT_CYAN + "]" + RESET + " " + value;
    }

    public static String renderProgressBar(double percentage, int barLength) {
        int filled = (int) Math.min(barLength, Math.max(0, (percentage / 100.0) * barLength));
        String color = percentage >= 90.0 ? EMERALD_GREEN : (percentage >= 70.0 ? AMBER_YELLOW : CORAL_RED);
        return color + "[" + "=".repeat(filled) + SLATE_GRAY + ".".repeat(barLength - filled) + color + "] " + String.format("%.1f%%", percentage) + RESET;
    }
}
