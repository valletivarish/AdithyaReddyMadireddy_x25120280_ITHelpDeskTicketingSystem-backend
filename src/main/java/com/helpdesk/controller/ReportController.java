package com.helpdesk.controller;

import com.helpdesk.dto.ReportDTO;
import com.helpdesk.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for advanced analytics reports.
 * Provides agent performance, SLA compliance, department workload,
 * and weekly trend data for the helpdesk system.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** GET /api/reports - Generate comprehensive analytics report */
    @GetMapping
    public ResponseEntity<ReportDTO> getReport() {
        return ResponseEntity.ok(reportService.generateReport());
    }
}
