package com.example.epsnwtbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseholdDto {

    private Long id;
    private Integer floor;
    private Float squareFootage;
    private Integer apartmentNumber;

    // Optionally, include a reference to RealEstate or other fields as needed
    private Long realEstateId;

    public HouseholdDto(Long id, Integer floor, Float squareFootage, Integer apartmentNumber, Long realEstateId) {
        this.id = id;
        this.floor = floor;
        this.squareFootage = squareFootage;
        this.apartmentNumber = apartmentNumber;
        this.realEstateId = realEstateId;
    }
}
