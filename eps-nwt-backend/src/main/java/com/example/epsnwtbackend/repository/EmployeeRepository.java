package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.dto.EmployeeSearchDTO;
import com.example.epsnwtbackend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT new com.example.epsnwtbackend.dto.EmployeeSearchDTO(e.id, e.user.username, e.name, e.surname) " +
            "FROM Employee e " +
            "WHERE e.user.username LIKE %:username%")
    Page<EmployeeSearchDTO> findAllWithUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Employee findByUserId(@Param("userId") Long userId);
}
