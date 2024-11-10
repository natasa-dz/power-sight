package com.example.epsnwtbackend.controller;

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

    @GetMapping(path = "/search/{address}/{apartmentNumber}")
    public ResponseEntity<List<Household>> search(@PathVariable String address, @PathVariable int apartmentNumber) {
        List<Household> households = householdService.search(address, apartmentNumber);
        return ResponseEntity.ok(households);
    }

}
