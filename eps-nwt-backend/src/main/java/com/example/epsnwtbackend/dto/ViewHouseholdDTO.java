package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Meter;
import com.example.epsnwtbackend.model.RealEstate;
import com.example.epsnwtbackend.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewHouseholdDTO {

    private Long id;
    private Integer floor;
    private Float squareFootage;
    private Integer apartmentNumber;
    private Long ownerId;

    //data from real estate
    private String address;
    private String municipality;
    private String town;
}
