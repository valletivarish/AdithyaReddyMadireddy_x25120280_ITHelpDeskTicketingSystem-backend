package com.helpdesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.config.JwtTokenProvider;
import com.helpdesk.dto.TicketDTO;
import com.helpdesk.model.*;
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
 * Integration tests for the TicketController.
 * Tests ticket CRUD operations, filtering, and validation with JWT authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;
    private String adminToken;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
        agentRepository.deleteAll();
        departmentRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user and generate a JWT token for authentication
        testUser = User.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);
        authToken = "Bearer " + jwtTokenProvider.generateToken(testUser);

        // Create an admin user for update/delete operations
        adminUser = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = "Bearer " + jwtTokenProvider.generateToken(adminUser);
    }

    /** Test creating a ticket with valid data returns 201 Created. */
    @Test
    void createTicket_WithValidData_ShouldReturnCreated() throws Exception {
        TicketDTO dto = TicketDTO.builder()
                .title("Laptop not booting")
                .description("My laptop shows a blue screen on startup and does not proceed to the login screen")
                .priority("HIGH")
                .category("Hardware")
                .build();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Laptop not booting"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.userName").value("Test User"));
    }

    /** Test creating a ticket without title fails validation. */
    @Test
    void createTicket_WithBlankTitle_ShouldReturnBadRequest() throws Exception {
        TicketDTO dto = TicketDTO.builder()
                .title("")
                .description("Some description")
                .priority("HIGH")
                .category("Hardware")
                .build();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    /** Test creating a ticket without authentication returns 403. */
    @Test
    void createTicket_WithoutAuth_ShouldReturnForbidden() throws Exception {
        TicketDTO dto = TicketDTO.builder()
                .title("Test ticket")
                .description("Test description")
                .priority("LOW")
                .category("Software")
                .build();

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    /** Test retrieving all tickets returns 200 OK. */
    @Test
    void getAllTickets_ShouldReturnOk() throws Exception {
        // Create a test ticket
        Ticket ticket = Ticket.builder()
                .title("Test ticket")
                .description("Test description")
                .status(TicketStatus.OPEN)
                .priority(Priority.MEDIUM)
                .category("Software")
                .user(testUser)
                .build();
        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test ticket"));
    }

    /** Test retrieving a ticket by ID returns 200 OK. */
    @Test
    void getTicketById_ShouldReturnOk() throws Exception {
        Ticket ticket = Ticket.builder()
                .title("Specific ticket")
                .description("Test description")
                .status(TicketStatus.OPEN)
                .priority(Priority.LOW)
                .category("Network")
                .user(testUser)
                .build();
        ticket = ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/" + ticket.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Specific ticket"));
    }

    /** Test retrieving a non-existent ticket returns 404. */
    @Test
    void getTicketById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tickets/99999")
                        .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }

    /** Test updating a ticket status to RESOLVED. */
    @Test
    void updateTicket_ChangeStatus_ShouldReturnOk() throws Exception {
        Ticket ticket = Ticket.builder()
                .title("Fix server")
                .description("Server is down")
                .status(TicketStatus.IN_PROGRESS)
                .priority(Priority.CRITICAL)
                .category("Infrastructure")
                .user(testUser)
                .build();
        ticket = ticketRepository.save(ticket);

        TicketDTO updateDto = TicketDTO.builder()
                .status("RESOLVED")
                .build();

        mockMvc.perform(put("/api/tickets/" + ticket.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }

    /** Test deleting a ticket returns 204 No Content. */
    @Test
    void deleteTicket_ShouldReturnNoContent() throws Exception {
        Ticket ticket = Ticket.builder()
                .title("To be deleted")
                .description("This ticket will be deleted")
                .status(TicketStatus.OPEN)
                .priority(Priority.LOW)
                .category("Other")
                .user(testUser)
                .build();
        ticket = ticketRepository.save(ticket);

        mockMvc.perform(delete("/api/tickets/" + ticket.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }

    /** Test searching tickets by keyword. */
    @Test
    void searchTickets_ShouldReturnMatchingTickets() throws Exception {
        Ticket ticket = Ticket.builder()
                .title("Email not working")
                .description("Cannot send or receive emails since this morning")
                .status(TicketStatus.OPEN)
                .priority(Priority.HIGH)
                .category("Software")
                .user(testUser)
                .build();
        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/search")
                        .param("keyword", "email")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Email not working"));
    }
}
