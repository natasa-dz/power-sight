package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.enums.AppointmentStatus;
import com.example.epsnwtbackend.model.Appointment;
import com.example.epsnwtbackend.model.Employee;
import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.AppointmentRepository;
import com.example.epsnwtbackend.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    private static final LocalTime START_OF_DAY = LocalTime.of(8, 0);
    private static final LocalTime END_OF_DAY = LocalTime.of(16, 0);
    private static final LocalTime BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime BREAK_END = LocalTime.of(12, 30);


    public List<LocalDateTime> getAvailableSlots(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Appointment> existingAppointments = appointmentRepository
                .findByEmployeeAndStartTimeBetween(employee,
                        LocalDateTime.of(date, START_OF_DAY),
                        LocalDateTime.of(date, END_OF_DAY));

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime slotStart = LocalDateTime.of(date, START_OF_DAY);

        while (slotStart.isBefore(LocalDateTime.of(date, END_OF_DAY))) {
            LocalDateTime slotEnd = slotStart.plusMinutes(30);

            if (slotStart.toLocalTime().isBefore(BREAK_START) || slotEnd.toLocalTime().isAfter(BREAK_END)) {
                LocalDateTime finalSlotStart = slotStart;
                boolean isOccupied = existingAppointments.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.CREATED)
                        .anyMatch(a -> a.getStartTime().isBefore(slotEnd) && a.getEndTime().isAfter(finalSlotStart));
                if (!isOccupied) {
                    availableSlots.add(slotStart);
                }
            }

            slotStart = slotEnd;
        }

        return availableSlots;
    }

    @Transactional
    public void bookAppointment(Long employeeId, Long userId, LocalDateTime startTime, int slotCount) {
        if (slotCount <= 0) {
            throw new IllegalArgumentException("Slot count must be greater than zero");
        }

        LocalDateTime endTime = startTime.plusMinutes(30 * slotCount);

        // end time cant be after 16:00
        LocalDateTime latestAllowedEndTime = startTime.toLocalDate().atTime(16, 0);
        if (endTime.isAfter(latestAllowedEndTime)) {
            throw new RuntimeException("Appointment can not end after 16:00");
        }

        boolean isSlotAvailable = appointmentRepository
                .existsByEmployeeIdAndStartTimeLessThanAndEndTimeGreaterThan(employeeId, endTime, startTime);
        if (isSlotAvailable) {
            throw new RuntimeException("One or more slots in the requested range are already booked");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        User user = userService.getUserById(userId);

        Appointment appointment = new Appointment();
        appointment.setEmployee(employee);
        appointment.setUser(user);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.CREATED);

        appointmentRepository.save(appointment);
    }

    @Transactional
    public synchronized boolean tryBookAppointment(Long employeeId, Long userId, LocalDateTime startTime, int slotCount) {
        try {
            bookAppointment(employeeId, userId, startTime, slotCount);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Transactional
    public void cancelAppointments(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException("Employee not found");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Appointment> futureAppointments = appointmentRepository
                .findByEmployeeIdAndStatusAndStartTimeAfter(employeeId, AppointmentStatus.CREATED, now);

        for(Appointment appointment : futureAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
        }
    }
}
