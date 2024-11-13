package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnershipRequestRepository extends JpaRepository<OwnershipRequest, Long> {

    //TODO: REFACTOR TO WORK WITH ENUMS!
    List<OwnershipRequest> findByStatus(Status status);

}
