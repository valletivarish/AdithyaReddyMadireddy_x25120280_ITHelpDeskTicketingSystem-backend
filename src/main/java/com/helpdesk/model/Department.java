package com.helpdesk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Department entity representing organizational departments in the helpdesk system.
 * Each department can have multiple agents and tickets assigned to it.
 * Examples: IT Support, Network Operations, Software Development.
 */
@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique name of the department (e.g., IT Support, Network Ops) */
    @Column(nullable = false, unique = true)
    private String name;

    /** Detailed description of the department responsibilities */
    @Column(length = 500)
    private String description;

    /** Contact email for the department */
    private String contactEmail;

    /** Timestamp when the department was created */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
