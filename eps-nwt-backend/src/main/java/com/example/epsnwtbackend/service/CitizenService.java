package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.model.Citizen;
import com.example.epsnwtbackend.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CitizenService {

    @Autowired
    private CitizenRepository citizenRepository;

    public Citizen saveCitizen(Citizen citizen) {
        return citizenRepository.save(citizen);
    }

    public Page<CitizenSearchDTO> search(String username, Pageable pageable) {
        if (username == null || username.trim().isEmpty()) {
            return citizenRepository.findAll(pageable).map(
                    c -> new CitizenSearchDTO(c.getId(), c.getUsername())
            );
        }
        return citizenRepository.findAllWithUsername(username, pageable);
    }
}
