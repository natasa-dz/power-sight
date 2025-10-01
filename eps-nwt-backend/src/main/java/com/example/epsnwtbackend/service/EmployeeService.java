package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.dto.ViewEmployeeDTO;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository employeeRepository;

    @Caching(evict = {
            @CacheEvict(value = "employeeSearch", allEntries = true),
            @CacheEvict(value = "allEmployees", allEntries = true),
            @CacheEvict(value = "allEmployeesNoPagination", allEntries = true)
    })
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Cacheable(value = "allEmployees", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public CacheablePage<EmployeeSearchDTO> getAllEmployees(Pageable pageable) {
        Page<EmployeeSearchDTO> employees =   employeeRepository.findAll(pageable)
                .map(EmployeeSearchDTO::toDto);
        return new CacheablePage<EmployeeSearchDTO>(new ArrayList<>(employees.getContent()), employees.getTotalPages(), employees.getTotalElements());
    }

    @Cacheable(value = "allEmployeesNoPagination")
    public List<EmployeeSearchDTO> getAllEmployeesNoPagination() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeSearchDTO::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "employeeDetails", key = "#id")
    public ViewEmployeeDTO getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(ViewEmployeeDTO::toDto).orElse(null);
    }

    public Optional<Employee> getEmployeeEntity(Long id) {
        return employeeRepository.findById(id);
    }

    @Cacheable(value = "employeeSearch", key = "{#username, #pageable.pageNumber, #pageable.pageSize}")
    public CacheablePage<EmployeeSearchDTO> search(String username, Pageable pageable) {
        if (username == null || username.trim().isEmpty()) {
            Page<EmployeeSearchDTO> employees =  employeeRepository.findAll(pageable).map(
                    e -> new EmployeeSearchDTO(e.getId(), e.getUser().getUsername(), e.getName(), e.getSurname(), e.getUser().getId())
            );
            return new CacheablePage<EmployeeSearchDTO>(new ArrayList<>(employees.getContent()), employees.getTotalPages(), employees.getTotalElements());
        }
        Page<EmployeeSearchDTO> employees =   employeeRepository.findAllWithUsername(username, pageable);
        return new CacheablePage<EmployeeSearchDTO>(new ArrayList<>(employees.getContent()), employees.getTotalPages(), employees.getTotalElements());
    }

    @Cacheable(value = "employeeDetailsUserId", key = "#id")
    public Employee getEmployeeByUserId(Long id) {
        return employeeRepository.findByUserId(id);
    }
}
