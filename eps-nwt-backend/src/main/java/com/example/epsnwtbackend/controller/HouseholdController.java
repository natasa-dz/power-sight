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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Duration;
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
    public ResponseEntity<List<AvailabilityData>> getAvailability(
            @PathVariable String name, @PathVariable String timeRange) {
        String duration = parseTimeRange(timeRange);
        List<AvailabilityData> summary = influxService.getAvailabilityByTimeRange(name, duration);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    public String parseTimeRange(String timeRange) {
        if (timeRange.equalsIgnoreCase("3")) {
            return "3h";
        } else if (timeRange.equalsIgnoreCase("6")) {
            return "6h";
        } else if (timeRange.equalsIgnoreCase("12")) {
            return "12h";
        } else if (timeRange.equalsIgnoreCase("24")) {
            return "1d";
        } else if (timeRange.equalsIgnoreCase("week")) {
            return "7d";
        } else if (timeRange.equalsIgnoreCase("month")) {
            return "30d";
        } else if (timeRange.equalsIgnoreCase("3months")) {
            return "90d";
        } else if (timeRange.equalsIgnoreCase("year")) {
            return "365d";
        } else {
            throw new IllegalArgumentException("Invalid time range format: " + timeRange);
        }
    }

}
