package com.fixflow.repository;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for Ticket management.
 */
public interface TicketRepository {

    Optional<Ticket> findById(Long id);

    Ticket save(Ticket ticket);

    Ticket update(Ticket ticket);

    void delete(Long id);

    List<Ticket> findAll();

    List<Ticket> findByReporterId(Long reporterId);

    List<Ticket> findByTechnicianId(Long technicianId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByCategory(Category category);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> search(String keyword);

    void clear();
}
