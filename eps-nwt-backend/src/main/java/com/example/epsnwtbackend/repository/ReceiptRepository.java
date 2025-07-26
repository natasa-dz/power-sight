package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> getAllByHousehold_Owner_Id(Long ownerId);

    List<Receipt> getAllByHousehold_Id(Long householdId);

    List<Receipt> getAllByHousehold_AccessGranted_CitizenId(Long ownerId);
}
