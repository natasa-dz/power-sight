package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.AppointmentDTO;
import com.example.epsnwtbackend.model.Appointment;
import com.example.epsnwtbackend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/available-slots/{employeeId}")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<LocalDateTime> slots = appointmentService.getAvailableSlots(employeeId, date);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/get-employees-appointments-for-date/{employeeId}")
    public ResponseEntity<List<AppointmentDTO>> getEmployeesAppointmentsForDate(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentDTO> slots = appointmentService.getAppointments(employeeId, date);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> tryBookAppointment(
            @RequestParam Long employeeId,
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(defaultValue = "1") int timeSlotCount) {
        try {
            if (startTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(400).body("Appointment cannot be booked in the past.");
            }
            boolean success = appointmentService.tryBookAppointment(employeeId, userId, startTime, timeSlotCount);
            if (success) {
                return ResponseEntity.ok("Appointment booked successfully!");
            } else {
                return ResponseEntity.status(409).body("Failed to book appointment, please try again.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}

