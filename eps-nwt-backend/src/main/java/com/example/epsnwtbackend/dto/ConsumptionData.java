package com.example.epsnwtbackend.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ConsumptionData {
    private final Instant timestamp;
    private final Double consumption;

    public ConsumptionData(Instant timestamp, Double consumption) {
        this.timestamp = timestamp;
        this.consumption = consumption;
    }
}
