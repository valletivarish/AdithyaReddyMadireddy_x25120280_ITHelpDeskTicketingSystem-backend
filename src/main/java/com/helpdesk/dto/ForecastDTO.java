package com.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for ML-based ticket resolution time forecasting results.
 * Contains predicted resolution times and trend analysis data
 * generated using Apache Commons Math SimpleRegression.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDTO {

    /** Predicted average resolution time in hours for the next period */
    private double predictedResolutionTimeHours;

    /** Trend direction: INCREASING, DECREASING, or STABLE */
    private String trendDirection;

    /** R-squared value indicating model confidence (0 to 1) */
    private double confidenceScore;

    /** Slope of the regression line (positive = increasing resolution time) */
    private double slope;

    /** Intercept of the regression line */
    private double intercept;

    /** Number of historical data points used for the prediction */
    private int dataPointsUsed;

    /** Predicted ticket volume for the next 7 days */
    private List<DailyForecast> dailyForecasts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyForecast {
        private String date;
        private double predictedTickets;
        private double predictedResolutionHours;
    }
}
