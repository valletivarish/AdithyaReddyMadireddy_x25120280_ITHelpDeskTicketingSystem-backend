package com.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that the Spring application context loads successfully.
 * Uses the test profile with H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
class HelpdeskApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the application context starts without errors
    }
}
