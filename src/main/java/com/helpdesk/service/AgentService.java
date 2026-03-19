package com.helpdesk.service;

import com.helpdesk.dto.AgentDTO;
import com.helpdesk.dto.AgentResponseDTO;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.Agent;
import com.helpdesk.model.Department;
import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.repository.AgentRepository;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for agent profile management.
 * Handles CRUD operations for agents, linking user accounts to departments
 * with specializations. Also provides agent availability queries.
 */
@Service
public class AgentService {

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public AgentService(AgentRepository agentRepository,
                        UserRepository userRepository,
                        DepartmentRepository departmentRepository) {
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new agent profile linked to an existing user and department.
     * Validates that the user exists, department exists, and user does not already have an agent profile.
     */
    @Transactional
    public AgentResponseDTO createAgent(AgentDTO dto) {
        if (agentRepository.existsByUserIdAndActiveTrue(dto.getUserId())) {
            throw new BadRequestException("User already has an agent profile");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", dto.getDepartmentId()));

        Agent agent = Agent.builder()
                .user(user)
                .department(department)
                .specialization(dto.getSpecialization())
                .available(dto.isAvailable())
                .phone(dto.getPhone())
                .build();

        user.setRole(Role.AGENT);
        userRepository.save(user);

        Agent saved = agentRepository.save(agent);
        return mapToResponseDTO(saved);
    }

    /** Retrieves all active agents in the system. */
    @Transactional(readOnly = true)
    public List<AgentResponseDTO> getAllAgents() {
        return agentRepository.findByActiveTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves a single agent by ID. */
    @Transactional(readOnly = true)
    public AgentResponseDTO getAgentById(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
        return mapToResponseDTO(agent);
    }

    /** Retrieves active agents by department for filtering. */
    @Transactional(readOnly = true)
    public List<AgentResponseDTO> getAgentsByDepartment(Long departmentId) {
        return agentRepository.findByDepartmentIdAndActiveTrue(departmentId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves all currently available active agents. */
    @Transactional(readOnly = true)
    public List<AgentResponseDTO> getAvailableAgents() {
        return agentRepository.findByAvailableTrueAndActiveTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing agent profile.
     * Allows changing department, specialization, availability, and phone.
     */
    @Transactional
    public AgentResponseDTO updateAgent(Long id, AgentDTO dto) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", id));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", dto.getDepartmentId()));

        agent.setDepartment(department);
        agent.setSpecialization(dto.getSpecialization());
        agent.setAvailable(dto.isAvailable());
        agent.setPhone(dto.getPhone());

        Agent updated = agentRepository.save(agent);
        return mapToResponseDTO(updated);
    }

    /** Soft deletes an agent profile by setting active to false and reverting user role to USER. */
    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
        agent.setActive(false);
        agent.setAvailable(false);
        agentRepository.save(agent);

        User user = agent.getUser();
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    /** Maps Agent entity to AgentResponseDTO including user and department details. */
    private AgentResponseDTO mapToResponseDTO(Agent agent) {
        return AgentResponseDTO.builder()
                .id(agent.getId())
                .userId(agent.getUser().getId())
                .userName(agent.getUser().getFullName())
                .userEmail(agent.getUser().getEmail())
                .departmentId(agent.getDepartment().getId())
                .departmentName(agent.getDepartment().getName())
                .specialization(agent.getSpecialization())
                .available(agent.isAvailable())
                .phone(agent.getPhone())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }
}
