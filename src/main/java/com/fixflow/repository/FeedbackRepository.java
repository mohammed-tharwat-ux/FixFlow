package com.fixflow.repository;

import com.fixflow.model.Feedback;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for User Feedback.
 */
public interface FeedbackRepository {

    Optional<Feedback> findById(Long id);

    Optional<Feedback> findByTicketId(Long ticketId);

    Feedback save(Feedback feedback);

    List<Feedback> findAll();

    void clear();
}
