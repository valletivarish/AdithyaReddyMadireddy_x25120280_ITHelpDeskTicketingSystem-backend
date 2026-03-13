package com.helpdesk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent entity representing support agents who handle tickets.
 * Each agent belongs to a department and is linked to a user account.
 * Agents have specializations and availability status to support ticket routing.
 */
@Entity
@Table(name = "agents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user account linked to this agent profile */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** The department this agent belongs to */
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** Agent specialization area (e.g., Hardware, Software, Networking) */
    @Column(nullable = false)
    private String specialization;

    /** Whether the agent is currently available to take new tickets */
    @Column(nullable = false)
    private boolean available;

    /** Phone number for direct contact with the agent */
    private String phone;

    /** Timestamp when the agent profile was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the agent profile was last updated */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
