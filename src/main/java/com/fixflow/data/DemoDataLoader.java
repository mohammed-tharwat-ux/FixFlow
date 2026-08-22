package com.fixflow.data;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.User;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;

import java.time.LocalDateTime;

/**
 * Preloads realistic demo seed data for system demonstration.
 * All demo credentials use safe standard passwords.
 */
public class DemoDataLoader {

    public static final String DEFAULT_PASSWORD = "Password123!";

    public static void loadDemoData(UserService userService,
                                    TicketService ticketService,
                                    AssignmentService assignmentService,
                                    SLAService slaService) {
        // 1. Create Demo Users
        User admin = userService.registerUser("Sarah Admin", "admin", "admin@fixflow.com", DEFAULT_PASSWORD, Role.ADMIN);
        User tech1 = userService.registerUser("Bob Technician", "tech_bob", "tech@fixflow.com", DEFAULT_PASSWORD, Role.TECHNICIAN);
        User tech2 = userService.registerUser("Alice Technician", "tech_alice", "alice.tech@fixflow.com", DEFAULT_PASSWORD, Role.TECHNICIAN);
        User user1 = userService.registerUser("John User", "john_user", "user@fixflow.com", DEFAULT_PASSWORD, Role.USER);
        User user2 = userService.registerUser("Emily Watson", "emily_w", "emily@fixflow.com", DEFAULT_PASSWORD, Role.USER);

        // 2. Create Sample Tickets
        // Ticket 1: Projector not working (OPEN, HARDWARE, MEDIUM)
        ticketService.createTicket(
                user1.getId(),
                "Projector not working in Room 302",
                "The ceiling HDMI projector fails to turn on and displays a red lamp warning indicator.",
                Category.HARDWARE,
                "Building A, Room 302",
                Priority.MEDIUM
        );

        // Ticket 2: Internet outage (ASSIGNED -> IN_PROGRESS, NETWORK, CRITICAL)
        Ticket t2 = ticketService.createTicket(
                user2.getId(),
                "Total Internet outage on 2nd floor",
                "All ethernet and WiFi access points on the second floor are down.",
                Category.NETWORK,
                "Building B, Floor 2",
                Priority.CRITICAL
        );
        assignmentService.assignTechnician(t2.getId(), tech1.getId(), admin.getId());
        ticketService.startProgress(t2.getId(), tech1.getId());

        // Ticket 3: Broken AC (RESOLVED within SLA, FACILITY, HIGH)
        Ticket t3 = ticketService.createTicket(
                user1.getId(),
                "Broken air conditioner in Server Room",
                "Server room temperature reaching 32C due to AC unit failure.",
                Category.FACILITY,
                "Data Center 1",
                Priority.HIGH
        );
        assignmentService.assignTechnician(t3.getId(), tech2.getId(), admin.getId());
        ticketService.startProgress(t3.getId(), tech2.getId());
        ticketService.resolveTicket(t3.getId(), tech2.getId(), "Replaced compressor capacitor and refilled coolant.");

        // Ticket 4: Electrical spark (CLOSED, ELECTRICAL, HIGH)
        Ticket t4 = ticketService.createTicket(
                user2.getId(),
                "Electrical outlet sparking",
                "Wall socket near desk 14 is sparking when plugging in chargers.",
                Category.ELECTRICAL,
                "Building A, Room 104",
                Priority.HIGH
        );
        assignmentService.assignTechnician(t4.getId(), tech1.getId(), admin.getId());
        ticketService.startProgress(t4.getId(), tech1.getId());
        ticketService.resolveTicket(t4.getId(), tech1.getId(), "Replaced faulty socket and insulated wiring.");
        ticketService.closeTicket(t4.getId(), user2.getId());
    }
}
