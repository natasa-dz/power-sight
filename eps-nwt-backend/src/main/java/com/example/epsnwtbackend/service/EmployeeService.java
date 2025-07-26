package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository employeeRepository;

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Page<EmployeeSearchDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employee -> EmployeeSearchDTO.toDto(employee));
    }

    public List<EmployeeSearchDTO> getAllEmployeesNoPagination() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeSearchDTO::toDto)
                .collect(Collectors.toList());
    }



    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Page<EmployeeSearchDTO> search(String username, Pageable pageable) {
        if (username == null || username.trim().isEmpty()) {
            return employeeRepository.findAll(pageable).map(
                    e -> new EmployeeSearchDTO(e.getId(), e.getUser().getUsername(), e.getName(), e.getSurname())
            );
        }
        return employeeRepository.findAllWithUsername(username, pageable);
    }

    public Employee getEmployeeByUserId(Long id) {
        return employeeRepository.findByUserId(id);
    }
}
