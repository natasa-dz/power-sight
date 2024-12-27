package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.CreatedReceipts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatedReceiptsRepository extends JpaRepository<CreatedReceipts, Long> {
}
