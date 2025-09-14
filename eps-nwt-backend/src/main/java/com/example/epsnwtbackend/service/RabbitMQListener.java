package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.configuration.HeartbeatMonitor;
import com.example.epsnwtbackend.model.ConsumptionMessage;
import com.example.epsnwtbackend.model.HeartbeatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RabbitMQListener {

    @Autowired
    private InfluxService influxService;

    @Autowired
    HeartbeatMonitor heartbeatMonitor;

    @RabbitListener(id = "heartbeatExchange", concurrency = "2")
    public void receiveHeartbeat(Message message) {
        System.out.println("Received heartbeat message");
        String messageContent = new String(message.getBody());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HeartbeatMessage parsedMessage = objectMapper.readValue(messageContent, HeartbeatMessage.class);
            System.out.println("Parsed message object heartbeat: " + parsedMessage);
            heartbeatMonitor.updateHeartbeat(parsedMessage.getId(), parsedMessage.getTimestamp().toInstant());
            influxService.saveHeartbeat(parsedMessage.getId(), 1, parsedMessage.getTimestamp(), new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(id = "consumptionExchange", concurrency = "2")
    public void receiveConsumption(Message message) {
        String messageContent = new String(message.getBody());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConsumptionMessage parsedMessage = objectMapper.readValue(messageContent, ConsumptionMessage.class);
            System.out.println("Parsed message object consumption: " + parsedMessage);
            Map<String, String> tags = Map.of(
                    "Municipality", message.getMessageProperties().getConsumerQueue().split("_queue_")[1],
                    "Id", parsedMessage.getId()
            );
            influxService.saveConsumption("simulators", parsedMessage.getConsumption(), parsedMessage.getTimestamp(), tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
