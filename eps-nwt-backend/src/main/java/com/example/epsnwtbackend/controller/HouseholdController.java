package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.AvailabilityData;
import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.dto.ViewHouseholdDTO;
import com.example.epsnwtbackend.service.HouseholdService;
import com.example.epsnwtbackend.service.InfluxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/household")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private InfluxService influxService;

    @GetMapping(path = "/find-by-id/{id}")
    public ResponseEntity<ViewHouseholdDTO> findById(@PathVariable Long id) {
        try {
            ViewHouseholdDTO household = householdService.getHousehold(id);
            return ResponseEntity.ok(household);
        } catch (NoResourceFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // na katastru se u opstini unosi adresa ili broj parcele
    // za pretragu domacinstava je mozda najbolje opstina + adresa + broj stana
    @GetMapping(path = "/search/{municipality}/{address}")
    public ResponseEntity<Page<HouseholdSearchDTO>> search(@PathVariable String municipality,
           @PathVariable String address, @RequestParam(required = false) Integer apartmentNumber, Pageable pageable) {
        if (apartmentNumber != null && apartmentNumber < 0) {
            return ResponseEntity.badRequest().build();
        }
        Page<HouseholdSearchDTO> households = householdService.search(municipality, address,
                apartmentNumber, pageable);
        return ResponseEntity.ok(households);
    }

    @GetMapping(path = "/search-no-owner/{municipality}/{address}")
    public ResponseEntity<Page<HouseholdSearchDTO>> searchNoOwner(
            @PathVariable String municipality, @PathVariable String address,
            @RequestParam(required = false) Integer apartmentNumber, Pageable pageable) {
        if (apartmentNumber != null && apartmentNumber < 0) {
            return ResponseEntity.badRequest().build();
        }
        Page<HouseholdSearchDTO> households = householdService.searchNoOwner(municipality, address,
                apartmentNumber, pageable);
        return ResponseEntity.ok(households);
    }

    @GetMapping(value = "/availability/{name}/{timeRange}")
    public ResponseEntity<?> getAvailability(
            @PathVariable String name, @PathVariable String timeRange) {
        String duration = null;
        LocalDate[] dateRange = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = parseDateRange(timeRange);
            } else {
                duration = parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        List<AvailabilityData> summary;
        if (dateRange != null) {
            summary = influxService.getAvailabilityByDateRange(name, dateRange[0], dateRange[1]);
        } else {
            summary = influxService.getAvailabilityByTimeRange(name, duration);
        }

        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    public String parseTimeRange(String timeRange) {
        switch (timeRange.toLowerCase()) {
            case "3":
                return "3h";
            case "6":
                return "6h";
            case "12":
                return "12h";
            case "24":
                return "1d";
            case "week":
                return "7d";
            case "month":
                return "30d";
            case "3months":
                return "90d";
            case "year":
                return "365d";
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time range format!");
        }
    }

    public LocalDate[] parseDateRange(String timeRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        String[] dates = timeRange.split("-");

        try {
            LocalDate startDate = LocalDate.parse(dates[0].trim(), formatter);
            LocalDate endDate = LocalDate.parse(dates[1].trim(), formatter);
            if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must be at least 1 day!");
            }
            return new LocalDate[]{startDate, endDate};
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range format!");
        }
    }

}
