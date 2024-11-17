package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.RealEstateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RealEstateRequestRepository extends JpaRepository<RealEstateRequest, Long> {

    // treba kasnije r.owner.id
    @Query("SELECT r FROM RealEstateRequest r WHERE r.owner = :ownerId")
    List<RealEstateRequest> getAllForOwner(@Param("ownerId") Long ownerId);
}
