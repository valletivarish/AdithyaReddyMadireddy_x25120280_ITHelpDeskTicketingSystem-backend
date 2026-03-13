package com.helpdesk.repository;

import com.helpdesk.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Comment entity database operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Find all comments for a specific ticket, ordered by creation time */
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    /** Find all comments by a specific user */
    List<Comment> findByUserId(Long userId);

    /** Count comments for a specific ticket */
    long countByTicketId(Long ticketId);
}
