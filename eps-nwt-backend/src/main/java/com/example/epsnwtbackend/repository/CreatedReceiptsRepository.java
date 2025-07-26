package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.CreatedReceipts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreatedReceiptsRepository extends JpaRepository<CreatedReceipts, Long> {

    Optional<CreatedReceipts> findByYearAndMonth(int year, String month);
}
