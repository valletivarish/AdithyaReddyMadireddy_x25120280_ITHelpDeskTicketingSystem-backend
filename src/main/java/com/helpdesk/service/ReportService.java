package com.helpdesk.service;

import com.helpdesk.dto.ReportDTO;
import com.helpdesk.model.*;
import com.helpdesk.repository.AgentRepository;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating advanced analytics reports.
 * Aggregates ticket data to produce agent performance metrics,
 * department workload analysis, SLA compliance rates, and weekly trends.
 */
@Service
public class ReportService {

    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;

    public ReportService(TicketRepository ticketRepository,
                         AgentRepository agentRepository,
                         DepartmentRepository departmentRepository) {
        this.ticketRepository = ticketRepository;
        this.agentRepository = agentRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public ReportDTO generateReport() {
        List<Ticket> allTickets = ticketRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // SLA compliance
        long totalWithSla = allTickets.stream().filter(t -> t.getSlaDeadline() != null).count();
        long slaBreached = allTickets.stream()
                .filter(t -> t.getSlaDeadline() != null)
                .filter(t -> {
                    if (t.getResolvedAt() != null) {
                        return t.getResolvedAt().isAfter(t.getSlaDeadline());
                    }
                    return t.getStatus() != TicketStatus.RESOLVED
                            && t.getStatus() != TicketStatus.CLOSED
                            && now.isAfter(t.getSlaDeadline());
                })
                .count();
        double slaCompliance = totalWithSla > 0 ? ((totalWithSla - slaBreached) * 100.0 / totalWithSla) : 100.0;

        // Total resolved
        long totalResolved = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED)
                .count();

        // Average resolution time
        double avgHours = allTickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .mapToDouble(t -> {
                    double h = Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes() / 60.0;
                    return Math.max(h, 0.0);
                })
                .average()
                .orElse(0.0);

        // Agent performance
        List<ReportDTO.AgentPerformance> agentPerf = new ArrayList<>();
        List<Agent> agents = agentRepository.findAll();
        for (Agent agent : agents) {
            List<Ticket> agentTickets = allTickets.stream()
                    .filter(t -> t.getAgent() != null && t.getAgent().getId().equals(agent.getId()))
                    .collect(Collectors.toList());
            long resolved = agentTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED)
                    .count();
            double agentAvg = agentTickets.stream()
                    .filter(t -> t.getResolvedAt() != null)
                    .mapToDouble(t -> Math.max(Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes() / 60.0, 0))
                    .average()
                    .orElse(0.0);

            agentPerf.add(ReportDTO.AgentPerformance.builder()
                    .agentName(agent.getUser().getFullName())
                    .ticketsAssigned(agentTickets.size())
                    .ticketsResolved(resolved)
                    .avgResolutionHours(Math.round(agentAvg * 100.0) / 100.0)
                    .build());
        }

        // Department workload
        List<ReportDTO.DepartmentWorkload> deptWorkload = new ArrayList<>();
        List<Department> departments = departmentRepository.findAll();
        long totalTicketCount = allTickets.size();
        for (Department dept : departments) {
            List<Ticket> deptTickets = allTickets.stream()
                    .filter(t -> t.getDepartment() != null && t.getDepartment().getId().equals(dept.getId()))
                    .collect(Collectors.toList());
            long openCount = deptTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS)
                    .count();
            double loadPct = totalTicketCount > 0 ? (deptTickets.size() * 100.0 / totalTicketCount) : 0;

            deptWorkload.add(ReportDTO.DepartmentWorkload.builder()
                    .departmentName(dept.getName())
                    .openTickets(openCount)
                    .totalTickets(deptTickets.size())
                    .loadPercentage(Math.round(loadPct * 100.0) / 100.0)
                    .build());
        }

        // Weekly trend (last 7 days)
        List<ReportDTO.DailyCount> weeklyTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long created = allTickets.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(day))
                    .count();
            long resolvedDay = allTickets.stream()
                    .filter(t -> t.getResolvedAt() != null && t.getResolvedAt().toLocalDate().equals(day))
                    .count();
            weeklyTrend.add(ReportDTO.DailyCount.builder()
                    .date(day.format(fmt))
                    .created(created)
                    .resolved(resolvedDay)
                    .build());
        }

        return ReportDTO.builder()
                .slaComplianceRate(Math.round(slaCompliance * 100.0) / 100.0)
                .totalResolved(totalResolved)
                .slaBreached(slaBreached)
                .avgResolutionHours(Math.round(avgHours * 100.0) / 100.0)
                .agentPerformance(agentPerf)
                .departmentWorkload(deptWorkload)
                .weeklyTrend(weeklyTrend)
                .build();
    }
}
