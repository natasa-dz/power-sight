package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.enums.RealEstateRequestStatus;
import com.example.epsnwtbackend.model.RealEstateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealEstateRequestDTO {
    private Long id;
    private Long owner;
    private String address;
    private String municipality;
    private String town;
    private Integer floors;
    private RealEstateRequestStatus status;
    private Date createdAt;
    private Date finishedAt;
    private String adminNote;

    public static RealEstateRequestDTO mapToDTO(RealEstateRequest entity) {
        if (entity == null) {
            return null;
        }

        return new RealEstateRequestDTO(
                entity.getId(),
                entity.getOwner(),
                entity.getAddress(),
                entity.getMunicipality(),
                entity.getTown(),
                entity.getFloors(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getFinishedAt(),
                entity.getAdminNote()
        );
    }
}

