package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HouseholdService {

    @Autowired
    private HouseholdRepository householdRepository;

    public Household getHousehold(Long id) throws NoResourceFoundException {
        Optional<Household> reference = householdRepository.findById(id);
        if (reference.isPresent()) { return reference.get(); }
        throw new NoResourceFoundException(HttpMethod.GET, "Household with this id does not exist");
    }

    public List<Household> search(String address, int apartmentNumber) {
        List<Household> households = householdRepository.findAllOnAddress(address);
        if(apartmentNumber != 0) {
            List<Household> filteredHouseholds = new ArrayList<>();
            for (Household household : households) {
                if (household.getApartmentNumber().equals(apartmentNumber)) {
                    filteredHouseholds.add(household);
                }
            }
            return filteredHouseholds;
        }
        return households;
    }
}
