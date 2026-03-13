package com.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the IT Help Desk Ticketing System application.
 * This Spring Boot application provides a REST API for managing IT support tickets,
 * agents, departments, and comments with JWT-based authentication.
 */
@SpringBootApplication
public class HelpdeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpdeskApplication.class, args);
    }
}
