package com.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating and updating agent profiles.
 * Links a user account to a department with specialization details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDTO {

    /** ID of the user account to link as an agent */
    @NotNull(message = "User ID is required")
    private Long userId;

    /** ID of the department this agent belongs to */
    @NotNull(message = "Department ID is required")
    private Long departmentId;

    /** Agent specialization area (e.g., Hardware, Software, Networking) */
    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;

    /** Whether the agent is available to take new tickets */
    private boolean available;

    /** Optional phone number for direct contact */
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
}
