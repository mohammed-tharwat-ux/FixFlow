package com.fixflow.ui.views;

import com.fixflow.model.User;
import com.fixflow.ui.ConsoleTheme;

import java.time.format.DateTimeFormatter;

/**
 * Views for user profile and system settings.
 */
public class ProfileSettingsView {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void renderProfile(User currentUser) {
        ConsoleTheme.printAppShell(currentUser, "User Profile");

        System.out.println("  [ USER ACCOUNT INFORMATION ]");
        System.out.printf("  - User ID:           #%d\n", currentUser.getId());
        System.out.printf("  - Full Name:         %s\n", currentUser.getFullName());
        System.out.printf("  - Username:          %s\n", currentUser.getUsername());
        System.out.printf("  - Email Address:     %s\n", currentUser.getEmail());
        System.out.printf("  - Assigned Role:     %s\n", currentUser.getRole());
        System.out.printf("  - Account Status:    %s [ACTIVE]\n", currentUser.getStatus());
        System.out.printf("  - Registered Since:  %s\n", currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().format(dtf) : "N/A");
        System.out.printf("  - Last Profile Sync: %s\n", currentUser.getUpdatedAt() != null ? currentUser.getUpdatedAt().format(dtf) : "N/A");
        System.out.println("  - Password Hash:     [PROTECTED - PBKDF2-HMAC-SHA256 Salted]");
        System.out.println("  " + ConsoleTheme.DIVIDER_DOUBLE);
    }

    public void renderSettings(User currentUser) {
        ConsoleTheme.printAppShell(currentUser, "System Settings & Configuration");

        System.out.println("  [ FIXFLOW ENVIRONMENT SETTINGS ]");
        System.out.println("  - Runtime Platform:       Java 21 LTS (Temurin)");
        System.out.println("  - Build Tool:             Apache Maven 3.9+");
        System.out.println("  - Testing Framework:      JUnit Jupiter 5.10.2");
        System.out.println("  - Repository Layer:       In-Memory Thread-Safe Concurrent Repositories");
        System.out.println("  - Security Encryption:    PBKDF2WithHmacSHA256 (10,000 Iterations, 16-byte Salt)");
        System.out.println("  - Notification Engine:    In-System Real-Time Event Dispatcher");
        System.out.println("  - SLA Target Evaluation:  Active Multi-Tier Deadline Monitoring");
        System.out.println("  " + ConsoleTheme.DIVIDER_DOUBLE);
    }
}
