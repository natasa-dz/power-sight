package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ConsumptionData;
import com.example.epsnwtbackend.service.ConsumptionService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/consumption")
public class ConsumptionController {

    @Autowired
    private InfluxService influxService;

    @Autowired
    private ConsumptionService consumptionService;

    @GetMapping(value = "/{city}/{timeRange}")
    public ResponseEntity<?> getConsumptionForCity(@PathVariable String city, @PathVariable String timeRange) {
        LocalDateTime[] dateRange = null;
        String duration = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = consumptionService.parseDateRange(timeRange);
            } else {
                duration = consumptionService.parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Double summary;
        if (dateRange != null) {
            summary = influxService.getConsumptionForCityByDateRange(city, dateRange[0], dateRange[1]);
        } else {
            summary = influxService.getConsumptionForCityByTimeRange(city, duration);
        }
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    @GetMapping(value = "/graph/{city}/{timeRange}")
    public ResponseEntity<?> getGraphConsumptionForCity(@PathVariable String city, @PathVariable String timeRange) {
        return new ResponseEntity<>(this.consumptionService.getGraphConsumptionData(city, timeRange), HttpStatus.OK);
    }

        @GetMapping(value = "/municipalities")
    public ResponseEntity<?> getMunicipalities() {
        return new ResponseEntity<>(influxService.getMunicipalitiesFromInflux(), HttpStatus.OK);
    }

}
