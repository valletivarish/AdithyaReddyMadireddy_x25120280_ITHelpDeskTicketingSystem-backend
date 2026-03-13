package com.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating and updating tickets.
 * Enforces validation rules: title is required (max 200 chars),
 * description is required (max 5000 chars), priority and category are mandatory.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    /** Title summarizing the issue - required, max 200 characters */
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /** Detailed description of the problem - required, max 5000 characters */
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    /** Priority level: LOW, MEDIUM, HIGH, or CRITICAL */
    @NotNull(message = "Priority is required")
    private String priority;

    /** Issue category such as Hardware, Software, Network, or Access */
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /** Optional department ID to route the ticket */
    private Long departmentId;

    /** Optional agent ID to assign the ticket to a specific agent */
    private Long agentId;

    /** Status of the ticket - used for updates only */
    private String status;
}
