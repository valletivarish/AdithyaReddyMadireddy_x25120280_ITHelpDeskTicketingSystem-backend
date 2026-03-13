package com.helpdesk.repository;

import com.helpdesk.model.Priority;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Ticket entity database operations.
 * Provides custom queries for filtering, searching, and analytics.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Find all tickets with a specific status */
    List<Ticket> findByStatus(TicketStatus status);

    /** Find all tickets with a specific priority */
    List<Ticket> findByPriority(Priority priority);

    /** Find tickets assigned to a specific agent */
    List<Ticket> findByAgentId(Long agentId);

    /** Find tickets submitted by a specific user */
    List<Ticket> findByUserId(Long userId);

    /** Find tickets belonging to a specific department */
    List<Ticket> findByDepartmentId(Long departmentId);

    /** Find tickets by category for filtering */
    List<Ticket> findByCategory(String category);

    /** Count tickets by status for dashboard statistics */
    long countByStatus(TicketStatus status);

    /** Count tickets by priority for dashboard statistics */
    long countByPriority(Priority priority);

    /** Search tickets by title or description containing keyword - case insensitive */
    @Query("SELECT t FROM Ticket t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    /** Find resolved tickets with resolution timestamps for ML forecasting */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESOLVED' AND t.resolvedAt IS NOT NULL AND t.createdAt >= :since ORDER BY t.createdAt ASC")
    List<Ticket> findResolvedTicketsSince(@Param("since") LocalDateTime since);

    /** Count tickets created per day for volume forecasting */
    @Query("SELECT CAST(t.createdAt AS date), COUNT(t) FROM Ticket t WHERE t.createdAt >= :since GROUP BY CAST(t.createdAt AS date) ORDER BY CAST(t.createdAt AS date)")
    List<Object[]> countTicketsPerDaySince(@Param("since") LocalDateTime since);

    /** Count tickets by category for dashboard chart data */
    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countByCategory();

    /** Count tickets by department for dashboard chart data */
    @Query("SELECT t.department.name, COUNT(t) FROM Ticket t WHERE t.department IS NOT NULL GROUP BY t.department.name")
    List<Object[]> countByDepartmentName();

    /** Find tickets by status and priority for priority queue dashboard */
    List<Ticket> findByStatusAndPriorityOrderByCreatedAtAsc(TicketStatus status, Priority priority);

    /** Find tickets by multiple statuses for filtering */
    List<Ticket> findByStatusIn(List<TicketStatus> statuses);
}
