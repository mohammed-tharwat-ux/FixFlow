package com.fixflow.ui;

import java.util.Scanner;

/**
 * Renders the official Project Introduction & Splash Screen for FixFlow.
 * Includes DEPI supervision, presentation team, and graphic brand identity.
 */
public final class PresentationIntro {

    private PresentationIntro() {
        // Prevent instantiation
    }

    public static void displaySplashScreen(Scanner scanner) {
        System.out.println("\n" + ConsoleTheme.NAVY_BLUE + ConsoleTheme.DIVIDER_DOUBLE + ConsoleTheme.RESET);
        System.out.println("  " + ConsoleTheme.BOLD + ConsoleTheme.BRIGHT_CYAN + "DIGITAL EGYPT PIONEERS INITIATIVE (DEPI)" + ConsoleTheme.RESET);
        System.out.println("  Ministry of Communications & Information Technology (MCIT)");
        System.out.println(ConsoleTheme.NAVY_BLUE + ConsoleTheme.DIVIDER_DOUBLE + ConsoleTheme.RESET);

        // Display Brand Graphic Logo
        ConsoleTheme.printBrandLogo();

        System.out.println("  " + ConsoleTheme.BOLD + "Under the Supervision of:" + ConsoleTheme.RESET);
        System.out.println("  " + ConsoleTheme.SLATE_GRAY + "--------------------------------------------------" + ConsoleTheme.RESET);
        System.out.println("  * Team Dr/ Hacker");
        System.out.println("  * " + ConsoleTheme.BOLD + "Dr / Mina S. Younan" + ConsoleTheme.RESET);

        System.out.println("\n  " + ConsoleTheme.BOLD + "Presented by: Team Dr/ Hacker" + ConsoleTheme.RESET);
        System.out.println("  " + ConsoleTheme.SLATE_GRAY + "--------------------------------------------------" + ConsoleTheme.RESET);
        System.out.println("  - " + ConsoleTheme.BRIGHT_CYAN + "Mohamed Tharwat" + ConsoleTheme.RESET);
        System.out.println("  - " + ConsoleTheme.BRIGHT_CYAN + "Mariam Samy" + ConsoleTheme.RESET);
        System.out.println("  - " + ConsoleTheme.BRIGHT_CYAN + "Shahd Moaz" + ConsoleTheme.RESET);
        System.out.println("  - " + ConsoleTheme.BRIGHT_CYAN + "Salah Reda" + ConsoleTheme.RESET);

        System.out.println("\n" + ConsoleTheme.NAVY_BLUE + ConsoleTheme.DIVIDER_SINGLE + ConsoleTheme.RESET);
        System.out.println("  " + ConsoleTheme.BOLD + "Project Mission:" + ConsoleTheme.RESET);
        System.out.println("  \"FixFlow is a smart maintenance and incident management system designed");
        System.out.println("   to organize maintenance requests from reporting to resolution. It helps");
        System.out.println("   users submit incidents, administrators assign technicians, technicians");
        System.out.println("   manage repairs, and the system monitor priorities, SLA performance,");
        System.out.println("   notifications, feedback, and reports.\"");

        System.out.println("\n  " + ConsoleTheme.BOLD + "Software Testing Foundation:" + ConsoleTheme.RESET);
        System.out.println("  \"The system is designed not only to perform maintenance management, but");
        System.out.println("   also to demonstrate professional Software Testing practices including");
        System.out.println("   Unit Testing, Integration Testing, Regression Testing, Validation Testing,");
        System.out.println("   Negative Testing, Boundary Value Analysis, Equivalence Partitioning, and");
        System.out.println("   Security Testing.\"");

        System.out.println("\n" + ConsoleTheme.NAVY_BLUE + ConsoleTheme.DIVIDER_DOUBLE + ConsoleTheme.RESET);
        System.out.println("  " + ConsoleTheme.EMERALD_GREEN + ConsoleTheme.BOLD + "                [ PRESS ENTER TO START FIXFLOW ]                " + ConsoleTheme.RESET);
        System.out.println(ConsoleTheme.NAVY_BLUE + ConsoleTheme.DIVIDER_DOUBLE + ConsoleTheme.RESET);

        if (scanner != null && System.console() != null) {
            scanner.nextLine();
        }
    }
}
