package com.example.epsnwtbackend.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
public class AmqpConfig implements DeliverCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConfig.class);
    private final String host;
    private final Integer port;

    @Value("${rabbitmq.management.username}")
    private String username;

    @Value("${rabbitmq.management.password}")
    private String password;

    public AmqpConfig(Environment env) {
        this.host = env.getProperty("amqp.host");
        this.port = Integer.parseInt(Objects.requireNonNull(env.getProperty("amqp.port")));
    }

    @Bean
    public Channel amqpClient() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .basicAuthentication(username, password)
                .build();
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        /*Measurement measurement = objectMapper.readValue(message, Measurement.class);
        measurement.setTopic(delivery.getEnvelope().getRoutingKey());
        this.measurementService.save(measurement);*/
        LOGGER.info(message);
    }
}