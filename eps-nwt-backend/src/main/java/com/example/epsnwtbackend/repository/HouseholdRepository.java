package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {

    @Query("SELECT h FROM Household h JOIN h.realEstate r WHERE r.address LIKE %:address%")
    List<Household> findAllOnAddress(@Param("address") String address);
}
