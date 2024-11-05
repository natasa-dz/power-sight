package com.example.epsnwtbackend.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {

    @RabbitListener(queues = "heartbeat_queue")
    public void receiveHeartbeat(String message) {
        System.out.println("Recieved heartbeat: " + message);
    }


    @RabbitListener(queues = "consumption_queue")
    public void receiveConsumption(String message) {
        System.out.println("Recieved consumption: " + message);
    }
}
