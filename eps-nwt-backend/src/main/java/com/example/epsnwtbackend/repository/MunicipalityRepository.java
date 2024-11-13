package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {
    @Query("SELECT m.name FROM Municipality m WHERE m.city.id = :cityId")
    List<String> findForCity(@Param("cityId") Long cityId);
}
