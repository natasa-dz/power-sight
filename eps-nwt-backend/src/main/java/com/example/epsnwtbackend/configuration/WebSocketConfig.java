package com.example.epsnwtbackend.configuration;

import com.example.epsnwtbackend.dto.AggregatedAvailabilityData;
import com.example.epsnwtbackend.service.ConsumptionService;
import com.example.epsnwtbackend.service.HouseholdService;
import com.example.epsnwtbackend.service.InfluxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Autowired private HouseholdService householdService;
    @Autowired
    private ConsumptionService consumptionService;
    @Autowired
    private InfluxService influxService;
    @Autowired @Lazy
    private SimpMessagingTemplate template;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/socket-subscriber")
                .enableSimpleBroker("/data");
    }

    @Scheduled(fixedRate = 3000, initialDelay = 2000)
    public void streamData() throws JsonProcessingException {
        for (String simulatorId : householdService.getAllSimulatorIds()) {
            List<AggregatedAvailabilityData> data = householdService.getDataForGraph("simulator-" + simulatorId, "3");
            Map<String, Object> message = Map.of("data", data);
            template.convertAndSend(
                    "/data/graph/" + simulatorId,
                    message
            );
        }
    }

    @Scheduled(fixedRate = 3000, initialDelay = 1000)
    public void streamConsumptionData() throws JsonProcessingException {
        for (String city : influxService.getExistingCities()) {
            Map<String, Double> message = consumptionService.getGraphConsumptionData(city, "1");
            System.out.println(message);
            template.convertAndSend(
                    "/data/graph/" + city,
                    message
            );
        }
    }

}
