package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateRepository extends JpaRepository<RealEstate, Long> {
}
