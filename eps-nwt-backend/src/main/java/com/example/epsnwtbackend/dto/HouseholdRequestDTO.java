package com.example.epsnwtbackend.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdRequestDTO {
    private Integer floor;
    private Float squareFootage;
    private Integer apartmentNumber;
}
