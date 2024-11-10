package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import org.jetbrains.annotations.NotNull;
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

    public List<HouseholdSearchDTO> search(String municipality, String address, int apartmentNumber) {
        //all on address in municipality
        List<Household> householdEntities = householdRepository.findAllOnAddress(municipality, address);
        List<HouseholdSearchDTO> households = getHouseholdSearchDTOS(householdEntities);
        if(apartmentNumber == 0) { return households; }
        //filtering by apartment number
        List<HouseholdSearchDTO> filteredHouseholds = new ArrayList<>();
        for (HouseholdSearchDTO household : households) {
            if (household.getApartmentNumber().equals(apartmentNumber)) {
                filteredHouseholds.add(household);
            }
        }
        return filteredHouseholds;
    }

    @NotNull
    private static List<HouseholdSearchDTO> getHouseholdSearchDTOS(List<Household> householdEntities) {
        List<HouseholdSearchDTO> households = new ArrayList<>();
        for (Household household : householdEntities) {
            HouseholdSearchDTO householdDTO = new HouseholdSearchDTO();
            householdDTO.setId(household.getId());
            householdDTO.setFloor(household.getFloor());
            householdDTO.setOwnerId(household.getOwner().getId());
            householdDTO.setApartmentNumber(household.getApartmentNumber());
            householdDTO.setSquareFootage(household.getSquareFootage());
            householdDTO.setRealEstateId(household.getRealEstate().getId());
            households.add(householdDTO);
        }
        return households;
    }
}
