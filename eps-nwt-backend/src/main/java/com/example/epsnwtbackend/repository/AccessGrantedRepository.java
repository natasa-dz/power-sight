package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.AccessGranted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccessGrantedRepository extends JpaRepository<AccessGranted, Long> {
    @Query("SELECT a.citizenId FROM AccessGranted a WHERE a.household.id = :householdId")
    List<Long> findCitizenIdsByHouseholdId(@Param("householdId") Long householdId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AccessGranted a WHERE a.household.id = :householdId AND a.citizenId = :citizenId")
    void deleteByHouseholdIdAndCitizenId(@Param("householdId") Long householdId, @Param("citizenId") Long citizenId);
}
