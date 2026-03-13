package com.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for the dashboard summary data.
 * Aggregates ticket statistics, status distribution, priority breakdown,
 * and recent activity for the helpdesk overview page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long totalAgents;
    private long availableAgents;
    private long totalDepartments;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByDepartment;
    private double averageResolutionTimeHours;
}
