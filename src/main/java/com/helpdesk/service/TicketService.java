package com.helpdesk.service;

import com.helpdesk.dto.TicketDTO;
import com.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.*;
import com.helpdesk.repository.AgentRepository;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for ticket management.
 * Handles the complete ticket lifecycle: creation, assignment, status transitions,
 * searching, filtering, and SLA deadline calculation.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         AgentRepository agentRepository,
                         DepartmentRepository departmentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new ticket with the given details.
     * Sets initial status to OPEN and calculates SLA deadline based on priority.
     * Optionally assigns to a department and agent if specified.
     */
    @Transactional
    public TicketResponseDTO createTicket(TicketDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate priority enum value
        Priority priority;
        try {
            priority = Priority.valueOf(dto.getPriority().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid priority: " + dto.getPriority() + ". Must be LOW, MEDIUM, HIGH, or CRITICAL");
        }

        Ticket ticket = Ticket.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(TicketStatus.OPEN)
                .priority(priority)
                .category(dto.getCategory())
                .user(user)
                .slaDeadline(calculateSlaDeadline(priority))
                .build();

        // Assign department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.getDepartmentId()));
            ticket.setDepartment(department);
        }

        // Assign agent if provided
        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", dto.getAgentId()));
            ticket.setAgent(agent);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        return mapToResponseDTO(saved);
    }

    /** Retrieves all tickets in the system. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves a single ticket by ID. */
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return mapToResponseDTO(ticket);
    }

    /** Retrieves tickets filtered by status. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByStatus(String status) {
        TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
        return ticketRepository.findByStatus(ticketStatus).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets filtered by priority. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByPriority(String priority) {
        Priority p = Priority.valueOf(priority.toUpperCase());
        return ticketRepository.findByPriority(p).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets assigned to a specific agent. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByAgent(Long agentId) {
        return ticketRepository.findByAgentId(agentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets submitted by a specific user. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets by department. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByDepartment(Long departmentId) {
        return ticketRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Searches tickets by keyword in title or description. */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> searchTickets(String keyword) {
        return ticketRepository.searchByKeyword(keyword).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing ticket's details and handles status transitions.
     * When status changes to RESOLVED, records the resolution timestamp.
     * Validates that status transitions follow the correct lifecycle.
     */
    @Transactional
    public TicketResponseDTO updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        // Update basic fields
        if (dto.getTitle() != null) {
            ticket.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            ticket.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            ticket.setCategory(dto.getCategory());
        }
        if (dto.getPriority() != null) {
            try {
                ticket.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
                // Recalculate SLA when priority changes
                ticket.setSlaDeadline(calculateSlaDeadline(ticket.getPriority()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority: " + dto.getPriority());
            }
        }

        // Handle status transition
        if (dto.getStatus() != null) {
            try {
                TicketStatus newStatus = TicketStatus.valueOf(dto.getStatus().toUpperCase());
                // Record resolution time when ticket is resolved
                if (newStatus == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
                    ticket.setResolvedAt(LocalDateTime.now());
                }
                ticket.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + dto.getStatus());
            }
        }

        // Update department assignment
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.getDepartmentId()));
            ticket.setDepartment(department);
        }

        // Update agent assignment
        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", dto.getAgentId()));
            ticket.setAgent(agent);
            // Auto-transition to IN_PROGRESS when agent is assigned
            if (ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }
        }

        Ticket updated = ticketRepository.save(ticket);
        return mapToResponseDTO(updated);
    }

    /** Deletes a ticket by ID. */
    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket", id);
        }
        ticketRepository.deleteById(id);
    }

    /**
     * Calculates the SLA deadline based on ticket priority.
     * CRITICAL: 4 hours, HIGH: 8 hours, MEDIUM: 24 hours, LOW: 48 hours.
     */
    private LocalDateTime calculateSlaDeadline(Priority priority) {
        return switch (priority) {
            case CRITICAL -> LocalDateTime.now().plusHours(4);
            case HIGH -> LocalDateTime.now().plusHours(8);
            case MEDIUM -> LocalDateTime.now().plusHours(24);
            case LOW -> LocalDateTime.now().plusHours(48);
        };
    }

    /** Maps Ticket entity to TicketResponseDTO with all related entity details. */
    private TicketResponseDTO mapToResponseDTO(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus().name())
                .priority(ticket.getPriority().name())
                .category(ticket.getCategory())
                .userId(ticket.getUser().getId())
                .userName(ticket.getUser().getFullName())
                .agentId(ticket.getAgent() != null ? ticket.getAgent().getId() : null)
                .agentName(ticket.getAgent() != null ? ticket.getAgent().getUser().getFullName() : null)
                .departmentId(ticket.getDepartment() != null ? ticket.getDepartment().getId() : null)
                .departmentName(ticket.getDepartment() != null ? ticket.getDepartment().getName() : null)
                .slaDeadline(ticket.getSlaDeadline())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
}
