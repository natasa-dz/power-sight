package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.ConsumptionMessage;
import com.example.epsnwtbackend.model.HeartbeatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitMQListener {

    @Autowired
    private InfluxService influxService;

    @RabbitListener(id = "heartbeatExchange", concurrency = "2")
    public void recieveHeartbeat(Message message) {
        String messageContent = new String(message.getBody());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HeartbeatMessage parsedMessage = objectMapper.readValue(messageContent, HeartbeatMessage.class);
            System.out.println("Parsed message object heartbeat: " + parsedMessage);
            float value = parsedMessage.getStatus().equals("online") ? 1 : 0;
            influxService.saveHeartbeat(parsedMessage.getId(), value, parsedMessage.getTimestamp(), new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(id = "consumptionExchange", concurrency = "2")
    public void receiveConsumption(Message message) {
        String messageContent = new String(message.getBody());
        System.out.println("municipality: " + message.getMessageProperties().getConsumerQueue().split("_queue_")[1]);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConsumptionMessage parsedMessage = objectMapper.readValue(messageContent, ConsumptionMessage.class);
            System.out.println("Parsed message object consumption: " + parsedMessage);
            Map<String, String> tags = Map.of(
                    "Municipality", message.getMessageProperties().getConsumerQueue().split("_queue_")[1]
            );
            influxService.saveConsumption(parsedMessage.getId(), parsedMessage.getConsumption(), parsedMessage.getTimestamp(), tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
