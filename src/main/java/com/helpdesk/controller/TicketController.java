package com.helpdesk.controller;

import com.helpdesk.dto.TicketDTO;
import com.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.model.User;
import com.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ticket management endpoints.
 * Provides full CRUD operations plus filtering, searching, and status management.
 * The authenticated user is automatically set as the ticket creator.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * POST /api/tickets - Create a new support ticket.
     * The authenticated user is automatically set as the ticket submitter.
     */
    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketDTO dto,
            @AuthenticationPrincipal User user) {
        TicketResponseDTO response = ticketService.createTicket(dto, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** GET /api/tickets - Retrieve all tickets. */
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    /** GET /api/tickets/{id} - Retrieve a specific ticket by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    /** GET /api/tickets/status/{status} - Filter tickets by status. */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    /** GET /api/tickets/priority/{priority} - Filter tickets by priority. */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
    }

    /** GET /api/tickets/agent/{agentId} - Get tickets assigned to an agent. */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(ticketService.getTicketsByAgent(agentId));
    }

    /** GET /api/tickets/user/{userId} - Get tickets submitted by a user. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(userId));
    }

    /** GET /api/tickets/department/{departmentId} - Get tickets by department. */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ticketService.getTicketsByDepartment(departmentId));
    }

    /** GET /api/tickets/search?keyword=xxx - Search tickets by keyword. */
    @GetMapping("/search")
    public ResponseEntity<List<TicketResponseDTO>> searchTickets(@RequestParam String keyword) {
        return ResponseEntity.ok(ticketService.searchTickets(keyword));
    }

    /**
     * PUT /api/tickets/{id} - Update an existing ticket.
     * Supports partial updates for title, description, priority, status, category,
     * department assignment, and agent assignment.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, dto));
    }

    /** DELETE /api/tickets/{id} - Delete a ticket. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
