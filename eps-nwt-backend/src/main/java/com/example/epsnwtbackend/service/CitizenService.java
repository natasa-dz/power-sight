package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.model.Citizen;
import com.example.epsnwtbackend.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CitizenService {

    @Autowired
    private CitizenRepository citizenRepository;

    @CacheEvict(value = "citizenSearch", allEntries = true)
    public Citizen saveCitizen(Citizen citizen) {
        return citizenRepository.save(citizen);
    }

    @Cacheable(value = "citizenSearch", key = "{#username, #pageable.pageNumber, #pageable.pageSize}")
    public CacheablePage<CitizenSearchDTO> search(String username, Pageable pageable) {
        if (username == null || username.trim().isEmpty()) {
            Page<CitizenSearchDTO> citizens = citizenRepository.findAll(pageable).map(
                    c -> new CitizenSearchDTO(c.getId(), c.getUsername())
            );
            return new CacheablePage<CitizenSearchDTO>(new ArrayList<>(citizens.getContent()), citizens.getTotalPages(), citizens.getTotalElements());
        }
        Page<CitizenSearchDTO> citizens = citizenRepository.findAllWithUsername(username, pageable);
        return new CacheablePage<CitizenSearchDTO>(new ArrayList<>(citizens.getContent()), citizens.getTotalPages(), citizens.getTotalElements());
    }
}
