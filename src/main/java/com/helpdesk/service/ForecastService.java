package com.helpdesk.service;

import com.helpdesk.dto.ForecastDTO;
import com.helpdesk.model.Ticket;
import com.helpdesk.repository.TicketRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ML-based forecasting service using Apache Commons Math SimpleRegression.
 * Provides ticket resolution time prediction and ticket volume forecasting
 * based on historical data analysis using linear regression.
 */
@Service
public class ForecastService {

    private final TicketRepository ticketRepository;

    public ForecastService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Generates a comprehensive forecast including:
     * - Predicted average resolution time for the next period
     * - Trend direction (increasing, decreasing, stable)
     * - Confidence score (R-squared)
     * - Daily forecasts for the next 7 days (ticket volume and resolution time)
     *
     * Uses SimpleRegression from Apache Commons Math to fit a linear model
     * on historical resolution time data, where x = day index and y = resolution hours.
     */
    @Transactional(readOnly = true)
    public ForecastDTO generateForecast() {
        // Fetch resolved tickets from the last 90 days for training data
        LocalDateTime since = LocalDateTime.now().minusDays(90);
        List<Ticket> resolvedTickets = ticketRepository.findResolvedTicketsSince(since);

        // If insufficient data, return default forecast with zero values
        if (resolvedTickets.size() < 3) {
            return buildDefaultForecast(resolvedTickets.size());
        }

        // Build regression model for resolution time prediction
        SimpleRegression resolutionRegression = new SimpleRegression();

        // Group tickets by day and calculate daily average resolution time
        Map<LocalDate, List<Double>> dailyResolutionTimes = new TreeMap<>();
        for (Ticket ticket : resolvedTickets) {
            LocalDate day = ticket.getCreatedAt().toLocalDate();
            double hours = Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt()).toMinutes() / 60.0;
            dailyResolutionTimes.computeIfAbsent(day, k -> new ArrayList<>()).add(hours);
        }

        // Add data points to the regression model (x=day index, y=avg resolution hours)
        int dayIndex = 0;
        for (Map.Entry<LocalDate, List<Double>> entry : dailyResolutionTimes.entrySet()) {
            double avgHours = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            resolutionRegression.addData(dayIndex, avgHours);
            dayIndex++;
        }

        // Build volume regression for ticket count prediction
        SimpleRegression volumeRegression = new SimpleRegression();
        Map<LocalDate, Long> dailyTicketCounts = new TreeMap<>();
        for (Ticket ticket : resolvedTickets) {
            LocalDate day = ticket.getCreatedAt().toLocalDate();
            dailyTicketCounts.merge(day, 1L, Long::sum);
        }

        int volIndex = 0;
        for (Map.Entry<LocalDate, Long> entry : dailyTicketCounts.entrySet()) {
            volumeRegression.addData(volIndex, entry.getValue());
            volIndex++;
        }

        // Calculate predictions for the next 7 days
        List<ForecastDTO.DailyForecast> dailyForecasts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 7; i++) {
            double predictedResHours = Math.max(0, resolutionRegression.predict(dayIndex + i));
            double predictedVolume = Math.max(0, volumeRegression.predict(volIndex + i));

            dailyForecasts.add(ForecastDTO.DailyForecast.builder()
                    .date(today.plusDays(i).format(formatter))
                    .predictedResolutionHours(Math.round(predictedResHours * 100.0) / 100.0)
                    .predictedTickets(Math.round(predictedVolume * 100.0) / 100.0)
                    .build());
        }

        // Determine trend direction based on the slope of the regression line
        double slope = resolutionRegression.getSlope();
        String trendDirection;
        if (slope > 0.1) {
            trendDirection = "INCREASING";
        } else if (slope < -0.1) {
            trendDirection = "DECREASING";
        } else {
            trendDirection = "STABLE";
        }

        // Calculate predicted resolution time for the next period
        double predictedResTime = Math.max(0, resolutionRegression.predict(dayIndex + 1));

        // R-squared indicates how well the model fits the data (0 to 1)
        double rSquared = resolutionRegression.getRSquare();
        if (Double.isNaN(rSquared)) {
            rSquared = 0.0;
        }

        return ForecastDTO.builder()
                .predictedResolutionTimeHours(Math.round(predictedResTime * 100.0) / 100.0)
                .trendDirection(trendDirection)
                .confidenceScore(Math.round(rSquared * 100.0) / 100.0)
                .slope(Math.round(slope * 1000.0) / 1000.0)
                .intercept(Math.round(resolutionRegression.getIntercept() * 100.0) / 100.0)
                .dataPointsUsed(resolvedTickets.size())
                .dailyForecasts(dailyForecasts)
                .build();
    }

    /**
     * Builds a default forecast when insufficient historical data is available.
     * Returns zeroed predictions with a message about needing more data.
     */
    private ForecastDTO buildDefaultForecast(int dataPoints) {
        List<ForecastDTO.DailyForecast> emptyForecasts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 7; i++) {
            emptyForecasts.add(ForecastDTO.DailyForecast.builder()
                    .date(today.plusDays(i).format(formatter))
                    .predictedResolutionHours(0)
                    .predictedTickets(0)
                    .build());
        }

        return ForecastDTO.builder()
                .predictedResolutionTimeHours(0)
                .trendDirection("STABLE")
                .confidenceScore(0)
                .slope(0)
                .intercept(0)
                .dataPointsUsed(dataPoints)
                .dailyForecasts(emptyForecasts)
                .build();
    }
}
