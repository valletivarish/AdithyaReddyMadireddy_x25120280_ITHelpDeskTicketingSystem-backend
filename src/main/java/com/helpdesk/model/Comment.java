package com.helpdesk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Comment entity representing notes or updates added to a ticket.
 * Both users and agents can add comments to track communication
 * and progress on a ticket throughout its lifecycle.
 */
@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The text content of the comment */
    @Column(nullable = false, length = 2000)
    private String content;

    /** The ticket this comment belongs to */
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** The user who wrote this comment */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Timestamp when the comment was posted */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
