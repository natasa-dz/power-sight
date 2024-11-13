package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}
