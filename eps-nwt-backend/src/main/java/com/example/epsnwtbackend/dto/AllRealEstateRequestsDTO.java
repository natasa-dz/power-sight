package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.enums.RealEstateRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AllRealEstateRequestsDTO {
    private Long id;
    private Long owner;
    private RealEstateRequestStatus status;
    private Date createdAt;
    private Date finishedAt;
    private String address;
    private String municipality;
    private String town;
}
