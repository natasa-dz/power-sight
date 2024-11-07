package com.example.epsnwtbackend.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {

    @RabbitListener(id = "heartbeatExchange", concurrency = "2")
    public void recieveHeartbeat(Object message) {
        System.out.println("Received heartbeat message: " + message);
    }

    @RabbitListener(id = "consumptionExchange", concurrency = "2")
    public void receiveConsumption(Object message) {
        System.out.println("Received consumption: " + message);
    }
}
