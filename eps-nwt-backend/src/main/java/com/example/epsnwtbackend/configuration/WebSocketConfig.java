package com.example.epsnwtbackend.configuration;

import com.example.epsnwtbackend.dto.AggregatedAvailabilityData;
import com.example.epsnwtbackend.service.HouseholdService;
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

    private final Map<String, WebSocketSession> activeSessions = new HashMap<>();

    @Autowired private HouseholdService householdService;
    @Autowired @Lazy
    private SimpMessagingTemplate template;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("http://localhost:4200")
                .addInterceptors(new SimulatorHandshakeInterceptor(activeSessions))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/socket-subscriber")
                .enableSimpleBroker("/data");
    }

    @Scheduled(fixedRate = 3000, initialDelay = 2000)
    public void streamData() throws JsonProcessingException {
        for (String simulatorId : this.activeSessions.keySet()) {
            WebSocketSession session = this.activeSessions.get(simulatorId);
            if (session.isOpen()) {
                List<AggregatedAvailabilityData> data = householdService.getDataForGraph(simulatorId, "3");
                        Map<String, Object> message = Map.of("data", data);
                        this.template.convertAndSendToUser(simulatorId,
                                String.format("/data/graph/%s", simulatorId),
                                this.mapper.writeValueAsString(message)
                        );
            }
            System.out.println("Measurement data published for " + simulatorId);
        }
    }

}
