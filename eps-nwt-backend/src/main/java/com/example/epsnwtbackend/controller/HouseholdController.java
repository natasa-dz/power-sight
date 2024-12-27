package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.*;
import com.example.epsnwtbackend.model.Household;
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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



    @GetMapping(path = "/no-owner")
    public ResponseEntity<Page<HouseholdDto>> getHouseholdsWithoutOwner(Pageable pageable) {
        Page<Household> households = householdService.noOwnerHouseholds(pageable);

        Page<HouseholdDto> householdDTOs = households.map(household -> new HouseholdDto(
                household.getId(),
                household.getFloor(),
                household.getSquareFootage(),
                household.getApartmentNumber(),
                household.getRealEstate().getId()
        ));

        return ResponseEntity.ok(householdDTOs);
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

    @GetMapping(value = "/graph/{name}/{timeRange}")
    public ResponseEntity<List<AggregatedAvailabilityData>> getDataForGraph(
            @PathVariable String name, @PathVariable String timeRange) {

        List<AggregatedAvailabilityData> data = householdService.getDataForGraph(name, timeRange);
        return ResponseEntity.ok(data);
    }

    @GetMapping(value = "/current/{name}")
    public ResponseEntity<Boolean> getCurrentStatus(
            @PathVariable String name) {
        boolean isOnline = householdService.getCurrentStatus(name);
        return ResponseEntity.ok(isOnline);
    }

    @GetMapping(value = "/availability/{name}/{timeRange}")
    public ResponseEntity<?> getAvailability(
            @PathVariable String name, @PathVariable String timeRange) {

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

        List<AvailabilityData> summary;
        if (dateRange != null) {
            summary = influxService.getAvailabilityByDateRange(name, dateRange[0], dateRange[1]);
        } else {
            summary = influxService.getAvailabilityByTimeRange(name, duration);
        }

        summary.sort(Comparator.comparing(AvailabilityData::getTimestamp));

        long onlineDuration = 0;
        long totalDuration = getSeconds(timeRange);
        final long interval = 15;

        if (duration != null) {
            Instant startInstant = Instant.now().minus(parseDuration(duration));
            int i = -1;
            for (AvailabilityData data : summary) {
                i++;
                Instant currentTime = data.getTimestamp();
                long timeSinceStart = ChronoUnit.SECONDS.between(startInstant, currentTime);

                if (timeSinceStart < 0) continue;

                long timeDifference = 0;
                if (i < summary.size() - 1) {
                    AvailabilityData next = summary.get(i + 1);
                    Instant nextTime = next.getTimestamp();
                    timeDifference = ChronoUnit.SECONDS.between(currentTime, nextTime);
                } else {
                    timeDifference = ChronoUnit.SECONDS.between(currentTime, Instant.now());
                }

                if (data.isOnline()) {
                    onlineDuration += Math.min(timeDifference, interval);
                }
            }

        } else {
            Instant endInstant = dateRange[1].atStartOfDay(ZoneOffset.ofHours(1)).plusDays(1).toInstant();

            for (int i = 0; i < summary.size() - 1; i++) {
                AvailabilityData current = summary.get(i);
                AvailabilityData next = summary.get(i + 1);

                Instant currentTime = current.getTimestamp();
                Instant nextTime = next.getTimestamp();

                long timeDifference = ChronoUnit.SECONDS.between(currentTime, nextTime);

                if (current.isOnline()) {
                    onlineDuration += Math.min(timeDifference, interval);
                }
            }
            if (!summary.isEmpty()) {
                AvailabilityData last = summary.get(summary.size() - 1);
                Instant lastTime = last.getTimestamp();
                long lastPeriod = ChronoUnit.SECONDS.between(lastTime, endInstant);

                if (last.isOnline()) {
                    onlineDuration += lastPeriod;
                }
            }
        }

        long offlineDuration = totalDuration - onlineDuration;

        double onlinePercentage = (onlineDuration * 100.0) / totalDuration;
        double offlinePercentage = 100.0 - onlinePercentage;

        Map<String, Object> response = new HashMap<>();
        response.put("onlinePercentage", onlinePercentage);
        response.put("offlinePercentage", offlinePercentage);
        response.put("onlineDuration", onlineDuration);
        response.put("offlineDuration", offlineDuration);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public String parseTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "3" -> "3h";
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

    private Duration parseDuration(String duration) {
        if (duration.endsWith("h")) {
            return Duration.parse("PT" + duration.toUpperCase());
        } else if (duration.endsWith("d")) {
            return Duration.parse("P" + duration.toUpperCase());
        } else if (duration.endsWith("m")) {
            return Duration.parse("PT" + duration.toUpperCase());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported duration format!");
        }
    }

    private Long getSeconds(String timeRange) {
        long secondsInDay = 24 * 60 * 60L;
        switch (timeRange) {
                case "3": return 3*60*60L;
                case "6": return 6*60*60L;
                case "12": return 12*60*60L;
                case "24": return secondsInDay;
                case "week": return 7*secondsInDay;
                case "month": return 30*secondsInDay;
                case "3months": return 90*secondsInDay;
                case "year": return 365*secondsInDay;
        }
        LocalDate[] dateRange = parseDateRange(timeRange);
        return (dateRange[1].toEpochDay() - dateRange[0].toEpochDay()) * secondsInDay;
    }

}
