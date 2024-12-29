package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.model.Citizen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    @Query("SELECT new com.example.epsnwtbackend.dto.CitizenSearchDTO(c.id, c.user.username) " +
            "FROM Citizen c " +
            "WHERE LOWER(c.user.username) LIKE LOWER(CONCAT(:username, '%'))")
    Page<CitizenSearchDTO> findAllWithUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM Citizen c WHERE c.user.id = :userId")
    Citizen findByUserId(@Param("userId") Long userId);
}
