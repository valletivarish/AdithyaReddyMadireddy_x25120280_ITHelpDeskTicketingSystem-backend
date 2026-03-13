package com.helpdesk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ticket entity representing a support request in the helpdesk system.
 * Tickets have a lifecycle: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED.
 * Each ticket is submitted by a user, optionally assigned to an agent,
 * and belongs to a department based on the issue category.
 */
@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short descriptive title summarizing the issue */
    @Column(nullable = false)
    private String title;

    /** Detailed description of the problem or request */
    @Column(nullable = false, length = 5000)
    private String description;

    /** Current status in the ticket lifecycle */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    /** Priority level determining urgency of resolution */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    /** Category of the issue (e.g., Hardware, Software, Network, Access) */
    @Column(nullable = false)
    private String category;

    /** The user who submitted this ticket */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The agent assigned to handle this ticket (nullable if unassigned) */
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    /** The department responsible for this type of issue */
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    /** SLA deadline by which the ticket should be resolved */
    private LocalDateTime slaDeadline;

    /** Timestamp when the ticket was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the ticket was last updated */
    private LocalDateTime updatedAt;

    /** Timestamp when the ticket was resolved (null if not yet resolved) */
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TicketStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
