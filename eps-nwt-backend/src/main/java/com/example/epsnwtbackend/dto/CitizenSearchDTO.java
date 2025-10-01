package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Citizen;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitizenSearchDTO {

    private Long id;
    private String username;
    private Long userId;

    public static CitizenSearchDTO toDto(Citizen citizen) {
        CitizenSearchDTO dto = new CitizenSearchDTO();
        dto.setId(citizen.getId());
        dto.setUsername(citizen.getUser().getUsername());
        dto.setUserId(citizen.getUser().getId());
        return dto;
    }
}
