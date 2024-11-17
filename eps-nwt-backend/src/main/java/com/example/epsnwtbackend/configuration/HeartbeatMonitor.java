package com.example.epsnwtbackend.configuration;

import com.example.epsnwtbackend.service.InfluxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HeartbeatMonitor {

    private final ConcurrentHashMap<String, Instant> lastHeartbeatMap = new ConcurrentHashMap<>();
    private final InfluxService influxService;

    public HeartbeatMonitor(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Scheduled(fixedRate = 15000)
    public void checkForOfflineSimulators() {
        Instant now = Instant.now();
        for (Map.Entry<String, Instant> entry : lastHeartbeatMap.entrySet()) {
            String simulatorId = entry.getKey();
            Instant lastHeartbeat = entry.getValue();

            if (Duration.between(lastHeartbeat, now).getSeconds() > 30) {
                influxService.saveHeartbeat(simulatorId, 0, Date.from(now), new HashMap<>());
                System.out.println("Simulator " + simulatorId + " marked as offline");
            }
        }
    }

    public void updateHeartbeat(String simulatorId, Instant timestamp) {
        lastHeartbeatMap.put(simulatorId, timestamp);
    }
}

