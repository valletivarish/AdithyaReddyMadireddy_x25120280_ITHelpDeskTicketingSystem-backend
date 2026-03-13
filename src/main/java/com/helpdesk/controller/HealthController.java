package com.helpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check controller for monitoring and CI/CD smoke tests.
 * Provides a public endpoint that returns the application status
 * without requiring authentication.
 */
@RestController
public class HealthController {

    /** GET /api/health - Returns application health status for monitoring and deployment verification. */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "IT Help Desk Ticketing System"
        ));
    }
}
