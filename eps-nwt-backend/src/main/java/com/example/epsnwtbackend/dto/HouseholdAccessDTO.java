package com.example.epsnwtbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdAccessDTO {
    private Long id;
    private Integer floor;
    private Float squareFootage;
    private Integer apartmentNumber;
    private Long ownerId;
    private List<Long> accessGranted;

    //data from real estate
    private String address;
    private String municipality;
    private String town;
}
