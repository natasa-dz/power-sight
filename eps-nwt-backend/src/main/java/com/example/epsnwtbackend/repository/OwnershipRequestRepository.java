package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OwnershipRequestRepository extends JpaRepository<OwnershipRequest, Long> {

    @Query("SELECT o FROM OwnershipRequest o WHERE o.status = :status")
    List<OwnershipRequest> findByStatus(Status status);
    @Query("SELECT o FROM OwnershipRequest o WHERE o.userId = :username")

    List<OwnershipRequest> findByUserId(String username);


    boolean existsByHouseholdIdAndUserId(Long householdId, String userId);

}
