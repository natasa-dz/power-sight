package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.model.Household;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {

    @Query("SELECT new com.example.epsnwtbackend.dto.HouseholdSearchDTO(h.id, h.floor, h.squareFootage, h.apartmentNumber, r.id, h.owner.id) " +
            "FROM Household h JOIN h.realEstate r " +
            "WHERE r.municipality = :municipality " +
            "AND r.address LIKE %:address% " +
            "AND (:apartmentNumber IS NULL OR h.apartmentNumber = :apartmentNumber)")
    Page<HouseholdSearchDTO> findAllOnAddress(
            @Param("municipality") String municipality,
            @Param("address") String address,
            @Param("apartmentNumber") Integer apartmentNumber,
            Pageable pageable);


    @Query("SELECT new com.example.epsnwtbackend.dto.HouseholdSearchDTO(h.id, h.floor, h.apartmentNumber, h.squareFootage, r.id) " +
            "FROM Household h JOIN h.realEstate r " +
            "WHERE r.municipality = :municipality " +
            "AND r.address LIKE %:address% " +
            "AND (h.owner IS NULL) " +
            "AND (:apartmentNumber IS NULL OR h.apartmentNumber = :apartmentNumber)")
    Page<HouseholdSearchDTO> findHouseholdsWithoutOwner(
            @Param("municipality") String municipality,
            @Param("address") String address,
            @Param("apartmentNumber") Integer apartmentNumber,
            Pageable pageable);


    @Query("SELECT h FROM Household h WHERE h.owner IS NULL ")
    Page<Household> searchNoOwner(Pageable pageable);

    @Query("SELECT h FROM Household h WHERE h.owner.id = :id ")
    List<Household> findForOwner(@Param("id") Long id);

    @Query("SELECT h FROM Household h WHERE h.owner.id= :id ")
    Page<Household> searchOwner(Pageable pageable, @Param("id") Long id);

    List<Household> findByOwnerIsNotNull();

}


