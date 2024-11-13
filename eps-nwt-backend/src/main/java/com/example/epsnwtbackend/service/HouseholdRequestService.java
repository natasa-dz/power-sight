package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.HouseholdRequestDTO;
import com.example.epsnwtbackend.model.HouseholdRequest;
import com.example.epsnwtbackend.repository.HouseholdRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HouseholdRequestService {

    @Autowired
    private HouseholdRequestRepository repository;

    public HouseholdRequest createHouseholdRequest(HouseholdRequestDTO requestDTO){
        HouseholdRequest request = new HouseholdRequest();
        request.setFloor(requestDTO.getFloor());
        request.setSquareFootage(requestDTO.getSquareFootage());
        request.setApartmentNumber(requestDTO.getApartmentNumber());
        repository.save(request);
        return request;
    }
}
