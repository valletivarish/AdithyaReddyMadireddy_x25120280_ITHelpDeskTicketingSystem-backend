package com.helpdesk.service;

import com.helpdesk.dto.DashboardDTO;
import com.helpdesk.model.Priority;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.TicketStatus;
import com.helpdesk.repository.AgentRepository;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for generating dashboard analytics and statistics.
 * Aggregates ticket data to provide overview metrics for the helpdesk,
 * including status distribution, priority breakdown, and resolution time averages.
 */
@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;

    public DashboardService(TicketRepository ticketRepository,
                            AgentRepository agentRepository,
                            DepartmentRepository departmentRepository) {
        this.ticketRepository = ticketRepository;
        this.agentRepository = agentRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Generates comprehensive dashboard statistics.
     * Includes ticket counts by status and priority, agent availability,
     * category and department distribution, and average resolution time.
     */
    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        // Count tickets by status for the status overview cards
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);
        long totalTickets = openTickets + inProgressTickets + resolvedTickets + closedTickets;

        // Count tickets by priority for priority breakdown chart
        Map<String, Long> ticketsByPriority = new HashMap<>();
        for (Priority p : Priority.values()) {
            ticketsByPriority.put(p.name(), ticketRepository.countByPriority(p));
        }

        // Count tickets by category for category distribution chart
        Map<String, Long> ticketsByCategory = new HashMap<>();
        List<Object[]> categoryResults = ticketRepository.countByCategory();
        for (Object[] result : categoryResults) {
            ticketsByCategory.put((String) result[0], (Long) result[1]);
        }

        // Count tickets by department for department distribution chart
        Map<String, Long> ticketsByDepartment = new HashMap<>();
        List<Object[]> deptResults = ticketRepository.countByDepartmentName();
        for (Object[] result : deptResults) {
            ticketsByDepartment.put((String) result[0], (Long) result[1]);
        }

        // Calculate average resolution time from resolved tickets in the last 90 days
        double avgResolutionHours = calculateAverageResolutionTime();

        return DashboardDTO.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .closedTickets(closedTickets)
                .totalAgents(agentRepository.count())
                .availableAgents(agentRepository.countByAvailableTrue())
                .totalDepartments(departmentRepository.count())
                .ticketsByPriority(ticketsByPriority)
                .ticketsByCategory(ticketsByCategory)
                .ticketsByDepartment(ticketsByDepartment)
                .averageResolutionTimeHours(avgResolutionHours)
                .build();
    }

    /**
     * Calculates the average resolution time in hours for tickets resolved in the last 90 days.
     * Resolution time is the duration between ticket creation and resolution.
     */
    private double calculateAverageResolutionTime() {
        LocalDateTime since = LocalDateTime.now().minusDays(90);
        List<Ticket> resolvedTickets = ticketRepository.findResolvedTicketsSince(since);

        if (resolvedTickets.isEmpty()) {
            return 0.0;
        }

        double totalHours = resolvedTickets.stream()
                .mapToDouble(ticket -> {
                    Duration duration = Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt());
                    double hours = duration.toMinutes() / 60.0;
                    return Math.max(hours, 0.0);
                })
                .sum();

        return Math.round((totalHours / resolvedTickets.size()) * 100.0) / 100.0;
    }
}
