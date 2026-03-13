package com.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for agent responses returned to the client.
 * Includes agent details along with linked user and department information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private boolean available;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
