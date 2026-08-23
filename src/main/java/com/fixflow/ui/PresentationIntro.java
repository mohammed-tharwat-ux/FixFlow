package com.fixflow.ui;

import java.util.Scanner;

/**
 * Renders the official Project Introduction & Splash Screen for FixFlow.
 */
public final class PresentationIntro {

    private PresentationIntro() {
        // Prevent instantiation
    }

    public static void displaySplashScreen(Scanner scanner) {
        System.out.println("\n" + ConsoleTheme.DIVIDER_DOUBLE);
        System.out.println("            DIGITAL EGYPT PIONEERS INITIATIVE (DEPI)            ");
        System.out.println("                   Ministry of Communications                   ");
        System.out.println("                 & Information Technology (MCIT)                ");
        System.out.println(ConsoleTheme.DIVIDER_DOUBLE);
        System.out.println("\n  Under the Supervision of:");
        System.out.println("  --------------------------------------------------");
        System.out.println("  Team Dr/ Hacker");
        System.out.println("  Dr / Mina S. Younan");
        System.out.println("\n  Presented by: Team Dr/ Hacker");
        System.out.println("  --------------------------------------------------");
        System.out.println("  * Mohamed Tharwat");
        System.out.println("  * Mariam Samy");
        System.out.println("  * Shahd Moaz");
        System.out.println("  * Salah Reda");

        System.out.println("\n" + ConsoleTheme.DIVIDER_SINGLE);
        System.out.println("  PROJECT: FIXFLOW");
        System.out.println("  Smart Maintenance & Incident Management System");
        System.out.println(ConsoleTheme.DIVIDER_SINGLE);

        System.out.println("\n  Project Mission:");
        System.out.println("  \"FixFlow is a smart maintenance and incident management system designed");
        System.out.println("   to organize maintenance requests from reporting to resolution. It helps");
        System.out.println("   users submit incidents, administrators assign technicians, technicians");
        System.out.println("   manage repairs, and the system monitor priorities, SLA performance,");
        System.out.println("   notifications, feedback, and reports.\"");

        System.out.println("\n  Software Testing Foundation:");
        System.out.println("  \"The system is designed not only to perform maintenance management, but");
        System.out.println("   also to demonstrate professional Software Testing practices including");
        System.out.println("   Unit Testing, Integration Testing, Regression Testing, Validation Testing,");
        System.out.println("   Negative Testing, Boundary Value Analysis, Equivalence Partitioning, and");
        System.out.println("   Security Testing.\"");

        System.out.println("\n" + ConsoleTheme.DIVIDER_DOUBLE);
        System.out.println("                  [ PRESS ENTER TO START FIXFLOW ]                      ");
        System.out.println(ConsoleTheme.DIVIDER_DOUBLE);

        if (scanner != null && System.console() != null) {
            scanner.nextLine();
        }
    }
}
