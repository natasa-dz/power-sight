package com.example.epsnwtbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdSearchDTO {
    private Long id;
    private Integer floor;
    private Float squareFootage;
    private Integer apartmentNumber;
    private Long realEstateId;
    private Long ownerId;

    public HouseholdSearchDTO(Long id, Integer floor, Integer apartmentNumber, Float squareFootage, Long realEstateId) {
        this.id = id;
        this.floor = floor;
        this.apartmentNumber = apartmentNumber;
        this.squareFootage = squareFootage;
        this.realEstateId = realEstateId;
    }
}
