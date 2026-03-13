package com.helpdesk.controller;

import com.helpdesk.dto.AgentDTO;
import com.helpdesk.dto.AgentResponseDTO;
import com.helpdesk.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for agent management endpoints.
 * Provides full CRUD operations for support agent profiles,
 * plus filtering by department and availability status.
 */
@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /** POST /api/agents - Create a new agent profile. */
    @PostMapping
    public ResponseEntity<AgentResponseDTO> createAgent(@Valid @RequestBody AgentDTO dto) {
        AgentResponseDTO response = agentService.createAgent(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** GET /api/agents - Retrieve all agents. */
    @GetMapping
    public ResponseEntity<List<AgentResponseDTO>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    /** GET /api/agents/{id} - Retrieve a specific agent by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<AgentResponseDTO> getAgentById(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    /** GET /api/agents/department/{departmentId} - Get agents by department. */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<AgentResponseDTO>> getAgentsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(agentService.getAgentsByDepartment(departmentId));
    }

    /** GET /api/agents/available - Get all currently available agents. */
    @GetMapping("/available")
    public ResponseEntity<List<AgentResponseDTO>> getAvailableAgents() {
        return ResponseEntity.ok(agentService.getAvailableAgents());
    }

    /** PUT /api/agents/{id} - Update an existing agent profile. */
    @PutMapping("/{id}")
    public ResponseEntity<AgentResponseDTO> updateAgent(
            @PathVariable Long id,
            @Valid @RequestBody AgentDTO dto) {
        return ResponseEntity.ok(agentService.updateAgent(id, dto));
    }

    /** DELETE /api/agents/{id} - Delete an agent profile. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
}
