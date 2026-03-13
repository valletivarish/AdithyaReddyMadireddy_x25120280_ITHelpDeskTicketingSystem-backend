package com.helpdesk.controller;

import com.helpdesk.dto.ForecastDTO;
import com.helpdesk.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for ML-based forecasting endpoints.
 * Provides ticket resolution time prediction and volume forecasting
 * using linear regression on historical data.
 */
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * GET /api/forecast - Generate ticket resolution time and volume forecast.
     * Returns predicted resolution times, trend analysis, confidence score,
     * and 7-day daily forecasts based on historical ticket data.
     */
    @GetMapping
    public ResponseEntity<ForecastDTO> getForecast() {
        return ResponseEntity.ok(forecastService.generateForecast());
    }
}
