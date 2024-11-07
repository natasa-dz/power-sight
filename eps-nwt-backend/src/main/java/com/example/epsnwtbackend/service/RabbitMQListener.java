package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.ConsumptionMessage;
import com.example.epsnwtbackend.model.HeartbeatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

@Service
public class RabbitMQListener {

    @RabbitListener(id = "heartbeatExchange", concurrency = "2")
    public void recieveHeartbeat(Message message) {
        String messageContent = new String(message.getBody());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HeartbeatMessage yourMessage = objectMapper.readValue(messageContent, HeartbeatMessage.class);
            System.out.println("Parsed message object heartbeat: " + yourMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(id = "consumptionExchange", concurrency = "2")
    public void receiveConsumption(Message message) {
        String messageContent = new String(message.getBody());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConsumptionMessage yourMessage = objectMapper.readValue(messageContent, ConsumptionMessage.class);
            System.out.println("Parsed message object consumption: " + yourMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
