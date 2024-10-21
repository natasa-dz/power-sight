package com.example.epsnwtbackend.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "meters")
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serialNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public Set<ConsumptionRecord> getConsumptionRecords() {
        return consumptionRecords;
    }

    public void setConsumptionRecords(Set<ConsumptionRecord> consumptionRecords) {
        this.consumptionRecords = consumptionRecords;
    }

    @OneToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsumptionRecord> consumptionRecords;

    // Getters and setters
}
