package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.dto.ViewHouseholdDTO;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ViewHouseholdDTO getHousehold(Long id) throws NoResourceFoundException {
        Optional<Household> reference = householdRepository.findById(id);
        if (reference.isPresent()) {
            Household household = reference.get();
            ViewHouseholdDTO viewHouseholdDTO = new ViewHouseholdDTO();
            viewHouseholdDTO.setId(household.getId());
            viewHouseholdDTO.setFloor(household.getFloor());
            viewHouseholdDTO.setApartmentNumber(household.getApartmentNumber());
            viewHouseholdDTO.setSquareFootage(household.getSquareFootage());

            viewHouseholdDTO.setOwnerId(household.getOwner() == null ? null : household.getOwner().getId());

            viewHouseholdDTO.setAddress(household.getRealEstate().getAddress());
            viewHouseholdDTO.setTown(household.getRealEstate().getTown());
            viewHouseholdDTO.setMunicipality(household.getRealEstate().getMunicipality());
            return viewHouseholdDTO;
        }
        throw new NoResourceFoundException(HttpMethod.GET, "Household with this id does not exist");
    }

    public Page<HouseholdSearchDTO> searchNoOwner(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        return householdRepository.findHouseholdsWithoutOwner(municipality, address, apartmentNumber, pageable);
    }

    public Page<HouseholdSearchDTO> search(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        return householdRepository.findAllOnAddress(municipality, address, apartmentNumber, pageable);
    }
}
