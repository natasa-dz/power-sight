package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestController
@RequestMapping("/household")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    @GetMapping(path = "/find-by-id/{id}")
    public ResponseEntity<Household> findById(@PathVariable Long id) {
        try {
            Household household = householdService.getHousehold(id);
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

}
