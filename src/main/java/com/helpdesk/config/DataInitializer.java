package com.helpdesk.config;

import com.helpdesk.model.*;
import com.helpdesk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Seeds demo data on application startup for development and testing.
 * Creates one user per role (ADMIN, AGENT, USER), departments, agents, and sample tickets.
 * Only runs if the database is empty (no users exist).
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       DepartmentRepository departmentRepository,
                                       AgentRepository agentRepository,
                                       TicketRepository ticketRepository) {
        return args -> {
            // Only seed if database is empty
            if (userRepository.count() > 0) {
                return;
            }

            // Create demo users for each role
            User admin = userRepository.save(User.builder()
                    .fullName("Admin User")
                    .email("admin@helpdesk.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());

            User agent = userRepository.save(User.builder()
                    .fullName("Agent User")
                    .email("agent@helpdesk.com")
                    .password(passwordEncoder.encode("agent123"))
                    .role(Role.AGENT)
                    .build());

            User user = userRepository.save(User.builder()
                    .fullName("Regular User")
                    .email("user@helpdesk.com")
                    .password(passwordEncoder.encode("user1234"))
                    .role(Role.USER)
                    .build());

            // Create departments
            Department itDept = departmentRepository.save(Department.builder()
                    .name("IT Support")
                    .description("Handles hardware and software issues")
                    .contactEmail("it@helpdesk.com")
                    .build());

            Department networkDept = departmentRepository.save(Department.builder()
                    .name("Network Operations")
                    .description("Manages network infrastructure and connectivity")
                    .contactEmail("network@helpdesk.com")
                    .build());

            Department securityDept = departmentRepository.save(Department.builder()
                    .name("Security")
                    .description("Handles access control and security incidents")
                    .contactEmail("security@helpdesk.com")
                    .build());

            // Create agent profiles
            Agent agentProfile = agentRepository.save(Agent.builder()
                    .user(agent)
                    .department(itDept)
                    .specialization("Software Troubleshooting")
                    .phone("+353 1 234 5678")
                    .available(true)
                    .build());

            // Create sample tickets with realistic timestamps
            LocalDateTime now = LocalDateTime.now();

            ticketRepository.save(Ticket.builder()
                    .title("Laptop not booting")
                    .description("My laptop shows a black screen after the company logo. Tried restarting multiple times.")
                    .priority(Priority.HIGH)
                    .status(TicketStatus.OPEN)
                    .category("Hardware")
                    .user(user)
                    .department(itDept)
                    .agent(agentProfile)
                    .createdAt(now.minusHours(3))
                    .updatedAt(now.minusHours(3))
                    .slaDeadline(now.plusHours(5))
                    .build());

            ticketRepository.save(Ticket.builder()
                    .title("Cannot access shared drive")
                    .description("Getting permission denied when trying to access the finance shared drive.")
                    .priority(Priority.MEDIUM)
                    .status(TicketStatus.IN_PROGRESS)
                    .category("Access")
                    .user(user)
                    .department(securityDept)
                    .createdAt(now.minusHours(12))
                    .updatedAt(now.minusHours(6))
                    .slaDeadline(now.plusHours(12))
                    .build());

            ticketRepository.save(Ticket.builder()
                    .title("Email not syncing on phone")
                    .description("Outlook on my phone stopped syncing emails since yesterday morning.")
                    .priority(Priority.LOW)
                    .status(TicketStatus.OPEN)
                    .category("Email")
                    .user(admin)
                    .department(itDept)
                    .createdAt(now.minusDays(1))
                    .updatedAt(now.minusDays(1))
                    .slaDeadline(now.plusHours(24))
                    .build());

            ticketRepository.save(Ticket.builder()
                    .title("VPN connection dropping")
                    .description("VPN disconnects every 15 minutes when working from home. ISP is fine.")
                    .priority(Priority.CRITICAL)
                    .status(TicketStatus.OPEN)
                    .category("Network")
                    .user(user)
                    .department(networkDept)
                    .createdAt(now.minusHours(1))
                    .updatedAt(now.minusHours(1))
                    .slaDeadline(now.plusHours(3))
                    .build());

            // Resolved tickets with proper createdAt < resolvedAt
            ticketRepository.save(Ticket.builder()
                    .title("Software license expired")
                    .description("Adobe Creative Suite license has expired. Need renewal for the design team.")
                    .priority(Priority.MEDIUM)
                    .status(TicketStatus.RESOLVED)
                    .category("Software")
                    .user(agent)
                    .department(itDept)
                    .agent(agentProfile)
                    .createdAt(now.minusDays(3))
                    .updatedAt(now.minusDays(1))
                    .resolvedAt(now.minusDays(1))
                    .slaDeadline(now.minusDays(2))
                    .build());

            ticketRepository.save(Ticket.builder()
                    .title("Printer not responding")
                    .description("3rd floor printer shows offline. Checked cables and power, everything looks fine.")
                    .priority(Priority.LOW)
                    .status(TicketStatus.RESOLVED)
                    .category("Hardware")
                    .user(user)
                    .department(itDept)
                    .agent(agentProfile)
                    .createdAt(now.minusDays(5))
                    .updatedAt(now.minusDays(3))
                    .resolvedAt(now.minusDays(3))
                    .slaDeadline(now.minusDays(3))
                    .build());

            ticketRepository.save(Ticket.builder()
                    .title("Password reset request")
                    .description("Locked out of my account after too many failed attempts. Need password reset.")
                    .priority(Priority.HIGH)
                    .status(TicketStatus.CLOSED)
                    .category("Access")
                    .user(admin)
                    .department(securityDept)
                    .agent(agentProfile)
                    .createdAt(now.minusDays(7))
                    .updatedAt(now.minusDays(6))
                    .resolvedAt(now.minusDays(6).plusHours(4))
                    .slaDeadline(now.minusDays(6))
                    .build());
        };
    }
}
