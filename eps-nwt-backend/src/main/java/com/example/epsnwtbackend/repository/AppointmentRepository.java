package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.enums.AppointmentStatus;
import com.example.epsnwtbackend.model.Appointment;
import com.example.epsnwtbackend.model.Employee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByEmployeeAndStartTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    boolean existsByEmployeeIdAndStartTimeLessThanAndEndTimeGreaterThan(Long employeeId, LocalDateTime endTime, LocalDateTime startTime);

    List<Appointment> findByEmployeeIdAndStartTimeAfter(Long employeeId, LocalDateTime startTime);

    List<Appointment> findByEmployeeIdAndStatusAndStartTimeAfter(Long employeeId, AppointmentStatus status, LocalDateTime startTime);

    //pessimistic lock
    //@Query(value = "SELECT a FROM Appointment a WHERE a.employee.id = :employeeId AND a.startTime < :endTime AND a.endTime > :startTime")
    //List<Appointment> findByEmployeeIdAndTimeSlotOverlapWithLock(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, LockModeType lockMode);

}
