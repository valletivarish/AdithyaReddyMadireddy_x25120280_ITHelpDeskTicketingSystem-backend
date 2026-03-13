package com.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating and updating departments.
 * Validates department name uniqueness and field constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    /** Department name - required, must be unique in the system */
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    /** Optional description of department responsibilities */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /** Optional contact email for the department - must be valid format if provided */
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;
}
