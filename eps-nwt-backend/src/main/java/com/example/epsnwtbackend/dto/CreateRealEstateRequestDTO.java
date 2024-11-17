package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.HouseholdRequest;
import com.example.epsnwtbackend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRealEstateRequestDTO {
    private Long owner;
    private String address;
    private String municipality;
    private String town;
    private String floors;
    private Date createdAt;
    private List<HouseholdRequestDTO> householdRequests;
}
