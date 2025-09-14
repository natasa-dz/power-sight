package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.dto.ViewEmployeeDTO;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.service.AppointmentService;
import com.example.epsnwtbackend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    AppointmentService appointmentService;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping(path = "/find-by-id/{id}")
    public ResponseEntity<ViewEmployeeDTO> findById(@PathVariable Long id) {
        ViewEmployeeDTO employee = employeeService.getEmployeeById(id);
        if(employee != null) {
            return ResponseEntity.ok(employee);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/find-by-user-id/{id}")
    public ResponseEntity<ViewEmployeeDTO> findByUserId(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeByUserId(id);
        if(employee!=null) {
            ViewEmployeeDTO viewEmployeeDTO = ViewEmployeeDTO.toDto(employee);
            return ResponseEntity.ok(viewEmployeeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/all-employees")
    public ResponseEntity<CacheablePage<EmployeeSearchDTO>> getAllEmployees(Pageable pageable) {
        CacheablePage<EmployeeSearchDTO> users = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/all-employees-no-pagination")
    public ResponseEntity<List<EmployeeSearchDTO>> getAllEmployeesNoPagination() {
        List<EmployeeSearchDTO> employees = employeeService.getAllEmployeesNoPagination();
        return ResponseEntity.ok(employees);
    }

     @GetMapping(path = "/search")
     public ResponseEntity<CacheablePage<EmployeeSearchDTO>> search(@RequestParam(value = "username", required = false, defaultValue = "") String username, Pageable pageable) {
         CacheablePage<EmployeeSearchDTO> users = employeeService.search(username, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/search/{username}")
    public ResponseEntity<CacheablePage<EmployeeSearchDTO>> search2(@PathVariable String username, Pageable pageable) {
        CacheablePage<EmployeeSearchDTO> users = employeeService.search(username, pageable);
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

    @Transactional
    @PutMapping(path = "/suspend/{employeeId}")
    public ResponseEntity<Boolean> suspendEmployee(@PathVariable Long employeeId) {
        Optional<Employee> employeeOptional = employeeService.getEmployeeEntity(employeeId);
        if(employeeOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Objects.requireNonNull(cacheManager.getCache("employeeSearch")).clear();
        Objects.requireNonNull(cacheManager.getCache("allEmployees")).clear();
        Objects.requireNonNull(cacheManager.getCache("allEmployeesNoPagination")).clear();
        Objects.requireNonNull(cacheManager.getCache("employeeDetails")).evict(employeeId);
        Objects.requireNonNull(cacheManager.getCache("employeeDetailsUserId")).evict(employeeOptional.get().getUser().getId());
        Employee employee = employeeOptional.get();
        employee.setSuspended(true);
        employeeService.saveEmployee(employee);
        appointmentService.cancelAppointments(employee.getId());
        return ResponseEntity.ok(true);
    }
}
