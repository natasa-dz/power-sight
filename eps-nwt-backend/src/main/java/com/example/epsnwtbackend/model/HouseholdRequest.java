package com.example.epsnwtbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "household_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer floor;

    @Column
    private Float squareFootage;

    @Column
    private Integer apartmentNumber;
}
