package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AppointmentRepositoryCustom {

    @Autowired
    private EntityManager entityManager;

    public List<Appointment> findByEmployeeIdAndTimeSlotOverlapWithLock(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        String query = "SELECT a FROM Appointment a WHERE a.employee.id = :employeeId AND a.startTime < :endTime AND a.endTime > :startTime";
        return entityManager.createQuery(query, Appointment.class)
                .setParameter("employeeId", employeeId)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Apply the pessimistic lock here
                .getResultList();
    }
}
