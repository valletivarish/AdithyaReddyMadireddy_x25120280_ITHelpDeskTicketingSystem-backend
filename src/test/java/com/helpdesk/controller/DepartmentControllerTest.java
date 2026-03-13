package com.helpdesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.config.JwtTokenProvider;
import com.helpdesk.dto.DepartmentDTO;
import com.helpdesk.model.Department;
import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the DepartmentController.
 * Tests department CRUD operations with validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
        agentRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build();
        admin = userRepository.save(admin);
        authToken = "Bearer " + jwtTokenProvider.generateToken(admin);
    }

    /** Test creating a department with valid data. */
    @Test
    void createDepartment_WithValidData_ShouldReturnCreated() throws Exception {
        DepartmentDTO dto = DepartmentDTO.builder()
                .name("IT Support")
                .description("General IT support and troubleshooting")
                .contactEmail("it@company.com")
                .build();

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("IT Support"));
    }

    /** Test creating a department with blank name fails validation. */
    @Test
    void createDepartment_WithBlankName_ShouldReturnBadRequest() throws Exception {
        DepartmentDTO dto = DepartmentDTO.builder()
                .name("")
                .description("Test")
                .build();

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /** Test creating a department with duplicate name fails. */
    @Test
    void createDepartment_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        Department existing = Department.builder()
                .name("IT Support")
                .description("Existing dept")
                .build();
        departmentRepository.save(existing);

        DepartmentDTO dto = DepartmentDTO.builder()
                .name("IT Support")
                .description("Duplicate")
                .build();

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /** Test retrieving all departments. */
    @Test
    void getAllDepartments_ShouldReturnOk() throws Exception {
        Department dept = Department.builder()
                .name("Network Ops")
                .description("Network operations")
                .build();
        departmentRepository.save(dept);

        mockMvc.perform(get("/api/departments")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Network Ops"));
    }

    /** Test updating a department. */
    @Test
    void updateDepartment_ShouldReturnOk() throws Exception {
        Department dept = Department.builder()
                .name("Old Name")
                .description("Old description")
                .build();
        dept = departmentRepository.save(dept);

        DepartmentDTO updateDto = DepartmentDTO.builder()
                .name("New Name")
                .description("Updated description")
                .contactEmail("new@company.com")
                .build();

        mockMvc.perform(put("/api/departments/" + dept.getId())
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    /** Test deleting a department. */
    @Test
    void deleteDepartment_ShouldReturnNoContent() throws Exception {
        Department dept = Department.builder()
                .name("To Delete")
                .description("Will be deleted")
                .build();
        dept = departmentRepository.save(dept);

        mockMvc.perform(delete("/api/departments/" + dept.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isNoContent());
    }
}
