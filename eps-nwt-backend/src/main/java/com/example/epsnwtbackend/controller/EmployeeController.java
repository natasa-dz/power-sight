package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.dto.ViewEmployeeDTO;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
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

    @PostMapping(path = "/image")
    public ResponseEntity<String> getProfileImage(@RequestBody String path) {
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(path));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return ResponseEntity.ok(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(path = "/suspend/{employeeId}")
    public ResponseEntity<Boolean> suspendEmployee(@PathVariable Long employeeId) {
        Optional<Employee> employeeOptional = employeeService.getEmployeeById(employeeId);
        if(employeeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Employee employee = employeeOptional.get();
        employee.setSuspended(true);
        employeeService.saveEmployee(employee);
        return ResponseEntity.ok(true);
    }
}
