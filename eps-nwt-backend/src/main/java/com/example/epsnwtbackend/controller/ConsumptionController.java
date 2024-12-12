package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ConsumptionData;
import com.example.epsnwtbackend.service.InfluxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/consumption")
public class ConsumptionController {

    @Autowired
    private InfluxService influxService;

    @GetMapping(value = "/{city}/{timeRange}")
    public ResponseEntity<?> getConsumptionForCity(@PathVariable String city, @PathVariable String timeRange) {

        System.out.println("pogodio kontroleeeeeeer consumption");
        LocalDate[] dateRange = null;
        String duration = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = parseDateRange(timeRange);
            } else {
                duration = parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Double summary;
        if (dateRange != null) {
            System.out.println("DATE RANGE");
            summary = influxService.getConsumptionForCityByDateRange(city, dateRange[0], dateRange[1]);
        } else {
            System.out.println("TIME RANGE");
            summary = influxService.getConsumptionForCityByTimeRange(city, duration);
        }
        System.out.println("SUMMARY KOJI VRACA");
        System.out.println(summary);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    private String parseTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "1" -> "1h";
            case "6" -> "6h";
            case "12" -> "12h";
            case "24" -> "1d";
            case "week" -> "7d";
            case "month" -> "30d";
            case "3months" -> "90d";
            case "year" -> "365d";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time range format!");
        };
    }

    private LocalDate[] parseDateRange(String timeRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        String[] dates = timeRange.split("-");

        try {
            LocalDate startDate = LocalDate.parse(dates[0].trim(), formatter);
            LocalDate endDate = LocalDate.parse(dates[1].trim(), formatter);
            if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must be at least 1 day!");
            }
            if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must not exceed 1 year!");
            }
            return new LocalDate[]{startDate, endDate};
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range format!");
        }
    }

}
