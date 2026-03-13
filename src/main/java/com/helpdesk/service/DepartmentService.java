package com.helpdesk.service;

import com.helpdesk.dto.DepartmentDTO;
import com.helpdesk.dto.DepartmentResponseDTO;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.Department;
import com.helpdesk.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for department management.
 * Handles CRUD operations for departments with validation
 * for unique department names.
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Constructor injection
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates a new department after validating name uniqueness.
     */
    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Department with name '" + dto.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .contactEmail(dto.getContactEmail())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToResponseDTO(saved);
    }

    /**
     * Retrieves all departments in the system.
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single department by ID.
     */
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return mapToResponseDTO(department);
    }

    /**
     * Updates an existing department's details.
     * Validates name uniqueness if the name is being changed.
     */
    @Transactional
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        // Check name uniqueness only if name is being changed
        if (!department.getName().equals(dto.getName()) && departmentRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Department with name '" + dto.getName() + "' already exists");
        }

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setContactEmail(dto.getContactEmail());

        Department updated = departmentRepository.save(department);
        return mapToResponseDTO(updated);
    }

    /**
     * Deletes a department by ID.
     */
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", id);
        }
        departmentRepository.deleteById(id);
    }

    /**
     * Maps Department entity to response DTO.
     */
    private DepartmentResponseDTO mapToResponseDTO(Department department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .contactEmail(department.getContactEmail())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
