package com.fixflow.service;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.TicketNotFoundException;
import com.fixflow.exception.UnauthorizedOperationException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import com.fixflow.repository.InMemoryNotificationRepository;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.InMemoryUserRepository;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AssignmentService Tests")
class AssignmentServiceTest {

    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private AssignmentService assignmentService;

    private User admin;
    private User tech1;
    private User tech2;
    private User regularUser;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketRepository = new InMemoryTicketRepository();
        userRepository = new InMemoryUserRepository();
        notificationService = new NotificationService(new InMemoryNotificationRepository());
        assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);

        admin = userRepository.save(User.builder().fullName("Admin").username("admin").email("admin@fixflow.com").role(Role.ADMIN).status(UserStatus.ACTIVE).build());
        tech1 = userRepository.save(User.builder().fullName("Tech 1").username("tech1").email("tech1@fixflow.com").role(Role.TECHNICIAN).status(UserStatus.ACTIVE).build());
        tech2 = userRepository.save(User.builder().fullName("Tech 2").username("tech2").email("tech2@fixflow.com").role(Role.TECHNICIAN).status(UserStatus.ACTIVE).build());
        regularUser = userRepository.save(User.builder().fullName("User").username("user").email("user@fixflow.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        ticket = ticketRepository.save(Ticket.builder()
                .reporter(regularUser)
                .title("Broken Chair")
                .description("Hydraulic cylinder broken")
                .category(Category.FACILITY)
                .location("Room 101")
                .priority(Priority.LOW)
                .status(TicketStatus.OPEN)
                .build());
    }

    @Test
    @DisplayName("Admin assigns ticket to active technician successfully")
    void shouldAssignTicketSuccessfully() {
        Ticket assigned = assignmentService.assignTechnician(ticket.getId(), tech1.getId(), admin.getId());

        assertEquals(TicketStatus.ASSIGNED, assigned.getStatus());
        assertNotNull(assigned.getAssignedTechnician());
        assertEquals(tech1.getId(), assigned.getAssignedTechnician().getId());
    }

    @Test
    @DisplayName("Reject assignment to inactive technician")
    void shouldRejectAssignmentToInactiveTechnician() {
        tech1.setStatus(UserStatus.INACTIVE);
        userRepository.update(tech1);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), tech1.getId(), admin.getId()));

        assertTrue(ex.getMessage().contains("inactive technician"));
    }

    @Test
    @DisplayName("Reject assignment to user without TECHNICIAN or ADMIN role")
    void shouldRejectAssignmentToNonTechnician() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), regularUser.getId(), admin.getId()));

        assertTrue(ex.getMessage().contains("must possess the TECHNICIAN"));
    }

    @Test
    @DisplayName("Reject assignment on CLOSED or CANCELLED tickets")
    void shouldRejectAssignmentOnClosedOrCancelledTicket() {
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.update(ticket);

        assertThrows(InvalidTicketStatusException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), tech1.getId(), admin.getId()));

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.update(ticket);

        assertThrows(InvalidTicketStatusException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), tech1.getId(), admin.getId()));
    }

    @Test
    @DisplayName("Non-admin user cannot assign tickets")
    void shouldRejectAssignmentByNonAdmin() {
        assertThrows(UnauthorizedOperationException.class, () ->
                assignmentService.assignTechnician(ticket.getId(), tech1.getId(), regularUser.getId()));
    }

    @Test
    @DisplayName("Get available active technicians and track workload")
    void shouldGetAvailableTechniciansAndWorkload() {
        List<User> available = assignmentService.getAvailableTechnicians();
        assertEquals(2, available.size());

        tech2.setStatus(UserStatus.INACTIVE);
        userRepository.update(tech2);
        assertEquals(1, assignmentService.getAvailableTechnicians().size());

        assertEquals(0, assignmentService.getTechnicianActiveWorkload(tech1.getId()));
        assignmentService.assignTechnician(ticket.getId(), tech1.getId(), admin.getId());
        assertEquals(1, assignmentService.getTechnicianActiveWorkload(tech1.getId()));
    }
}
