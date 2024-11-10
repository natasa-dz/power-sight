package com.example.epsnwtbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "meters")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serialNumber;

    @OneToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsumptionRecord> consumptionRecords;

}
