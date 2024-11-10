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
}
