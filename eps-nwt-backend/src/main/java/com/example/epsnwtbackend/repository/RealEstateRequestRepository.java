package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.RealEstateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateRequestRepository extends JpaRepository<RealEstateRequest, Long> {
}
