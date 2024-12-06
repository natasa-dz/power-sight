package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.dto.ViewEmployeeDTO;
import com.example.epsnwtbackend.dto.ViewHouseholdDTO;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @GetMapping(path = "/find-by-id/{id}")
    public ResponseEntity<ViewEmployeeDTO> findById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if(employee.isPresent()) {
            ViewEmployeeDTO viewEmployeeDTO = ViewEmployeeDTO.toDto(employee.get());
            return ResponseEntity.ok(viewEmployeeDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/search/{username}")
    public ResponseEntity<Page<EmployeeSearchDTO>> search(@PathVariable String username, Pageable pageable) {
        Page<EmployeeSearchDTO> users = employeeService.search(username, pageable);
        return ResponseEntity.ok(users);
    }
}
