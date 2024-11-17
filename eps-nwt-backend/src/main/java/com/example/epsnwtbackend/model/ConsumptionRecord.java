package com.example.epsnwtbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_records")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConsumptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(nullable = false)
    private double consumption; // Consumption in kWh

    @Column(nullable = false)
    private LocalDateTime recordedAt;

}
