package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.enums.AppointmentStatus;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {
    private Long id;

    private Long employeeId;

    private String employeeUsername;

    private Long userId;

    private String usersUsername;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private AppointmentStatus status;
}
