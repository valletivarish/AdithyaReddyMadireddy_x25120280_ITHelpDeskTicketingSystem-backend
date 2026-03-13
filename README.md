# IT Help Desk Ticketing System - Backend

Student: Adithya Reddy Madireddy
Student ID: x25120280
Module: Cloud DevOpsSec (H9CDOS)

## Project Description

Backend REST API for an IT Help Desk Ticketing System built with Spring Boot 3. Provides ticket management with full CRUD operations, JWT authentication, role-based access control, and ML-based ticket resolution time forecasting.

## Tech Stack

- Java 17 with Spring Boot 3.2
- Spring Security 6 with JWT authentication
- Spring Data JPA with PostgreSQL
- Apache Commons Math 3 for ML forecasting (SimpleRegression)
- Springdoc OpenAPI for Swagger documentation
- Maven for build and dependency management

## Entities

- User: Authentication and profile management with roles (ADMIN, AGENT, USER)
- Department: Organizational departments for ticket routing
- Agent: Support agent profiles linked to users and departments
- Ticket: Support tickets with status lifecycle (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
- Comment: Notes and updates on tickets

## API Endpoints

### Authentication (Public)
- POST /api/auth/register - Register new user
- POST /api/auth/login - Login and get JWT token

### Tickets (Authenticated)
- GET/POST /api/tickets - List all / Create new ticket
- GET/PUT/DELETE /api/tickets/{id} - Get / Update / Delete ticket
- GET /api/tickets/status/{status} - Filter by status
- GET /api/tickets/priority/{priority} - Filter by priority
- GET /api/tickets/search?keyword=xxx - Search tickets
- GET /api/tickets/agent/{agentId} - Tickets by agent
- GET /api/tickets/user/{userId} - Tickets by user
- GET /api/tickets/department/{deptId} - Tickets by department

### Departments, Agents, Comments (Authenticated)
- Full CRUD on /api/departments, /api/agents, /api/comments

### Analytics (Authenticated)
- GET /api/dashboard - Dashboard statistics
- GET /api/forecast - ML-based ticket resolution time prediction

### Health Check (Public)
- GET /api/health - Application health status

## Running Locally

1. Ensure PostgreSQL is running with database helpdesk_db
2. Update credentials in application.properties if needed
3. Run: mvn spring-boot:run
4. Access Swagger UI: http://localhost:8080/swagger-ui.html

## Static Analysis

- SpotBugs: Bug detection (runs during mvn verify)
- PMD: Code style and complexity analysis (runs during mvn verify)
- JaCoCo: Code coverage with 60% minimum threshold
- Semgrep: Security vulnerability scanning (runs in CI/CD pipeline)

## Running Tests

mvn test (uses H2 in-memory database)

## CI/CD

GitHub Actions pipeline at .github/workflows/ci-cd.yml
- CI: Build, test, static analysis, security scanning
- CD: Deploy to AWS EC2 via SSH
