package com.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for advanced analytics reports.
 * Contains agent performance, department workload, SLA compliance,
 * and weekly trend data for the helpdesk system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    /** SLA compliance rate as percentage (0-100) */
    private double slaComplianceRate;

    /** Total tickets resolved in the reporting period */
    private long totalResolved;

    /** Total tickets breaching SLA deadline */
    private long slaBreached;

    /** Average first response time in hours */
    private double avgResolutionHours;

    /** Agent performance metrics */
    private List<AgentPerformance> agentPerformance;

    /** Department workload distribution */
    private List<DepartmentWorkload> departmentWorkload;

    /** Daily ticket counts for the last 7 days */
    private List<DailyCount> weeklyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentPerformance {
        private String agentName;
        private long ticketsResolved;
        private long ticketsAssigned;
        private double avgResolutionHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentWorkload {
        private String departmentName;
        private long openTickets;
        private long totalTickets;
        private double loadPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long created;
        private long resolved;
    }
}
