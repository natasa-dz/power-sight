package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewEmployeeDTO {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String userPhoto;
    private Long userId;
    private boolean suspended;

    public static ViewEmployeeDTO toDto(Employee employee) {
        ViewEmployeeDTO dto = new ViewEmployeeDTO();
        dto.setId(employee.getId());
        dto.setUsername(employee.getUser().getUsername());
        dto.setName(employee.getName());
        dto.setSurname(employee.getSurname());
        dto.setUserPhoto(employee.getUser().getUserPhoto());
        dto.setUserId(employee.getUser().getId());
        dto.setSuspended(employee.getSuspended());
        return dto;
    }
}
