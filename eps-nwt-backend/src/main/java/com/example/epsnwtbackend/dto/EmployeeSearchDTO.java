package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSearchDTO {
    private Long id;
    private String username;
    private String name;
    private String surname;

    public static EmployeeSearchDTO toDto(Employee employee) {
        EmployeeSearchDTO dto = new EmployeeSearchDTO();
        dto.setId(employee.getId());
        dto.setUsername(employee.getUser().getUsername());
        dto.setName(employee.getName());
        dto.setSurname(employee.getSurname());
        return dto;
    }
}
