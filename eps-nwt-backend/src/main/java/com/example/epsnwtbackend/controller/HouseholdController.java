package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.service.HouseholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    // if apartment number is 0 than all on address are returned
    @GetMapping(path = "/search/{municipality}/{address}/{apartmentNumber}")
    public ResponseEntity<List<HouseholdSearchDTO>> search(@PathVariable String municipality, @PathVariable String address, @PathVariable int apartmentNumber) {
        if(apartmentNumber < 0) return ResponseEntity.badRequest().build();
        List<HouseholdSearchDTO> households = householdService.search(municipality, address, apartmentNumber);
        return ResponseEntity.ok(households);
    }

}
