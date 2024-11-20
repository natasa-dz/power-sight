package com.example.epsnwtbackend.dto;

import java.time.Instant;

public class AvailabilityData {
    private final Instant timestamp;
    private final boolean isOnline;

    public AvailabilityData(Instant timestamp, boolean isOnline) {
        this.timestamp = timestamp;
        this.isOnline = isOnline;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isOnline() {
        return isOnline;
    }
}