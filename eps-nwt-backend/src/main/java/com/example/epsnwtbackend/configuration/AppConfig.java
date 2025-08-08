package com.example.epsnwtbackend.configuration;

import com.example.epsnwtbackend.service.RabbitQueueService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.*;

@Configuration
public class AppConfig {

    @Value("${rabbitmq.management.username}")
    private String username;

    @Value("${rabbitmq.management.password}")
    private String password;

    @Value("${rabbitmq.api.url}")
    private String apiUrl;

    private List<String> heartbeatQueueNames = Collections.emptyList();
    private List<String> consumptionQueueNames = Collections.emptyList();

    @Autowired
    RabbitQueueService rabbitQueueService;

    @Autowired
    private RestTemplate restTemplate;

    public List<String> getHeartbeatQueues(RestTemplate restTemplate) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .build().toUriString();

        // Set up authentication headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Wrap headers in an HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the API request with authentication headers
        try {

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url + "/queues",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> queues = response.getBody();

            return queues.stream()
                    .map(q -> (String) q.get("name"))
                    .filter(name -> name.startsWith("heartbeat_queue_"))
                    .toList();
        } catch (HttpClientErrorException e) {
            // Handle error (e.g. unauthorized)
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> getConsumptionQueues(RestTemplate restTemplate) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .build().toUriString();

        // Set up authentication headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Wrap headers in an HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the API request with authentication headers
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url + "/queues",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> queues = response.getBody();

            return queues.stream()
                    .map(q -> (String) q.get("name"))
                    .filter(name -> name.startsWith("consumption_queue_"))
                    .toList();
        } catch (HttpClientErrorException e) {
            // Handle error (e.g. unauthorized)
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void updateQueues() {

        Set<String> newHeartbeatQueues = new HashSet<>(getHeartbeatQueues(restTemplate));
        Set<String> newConsumptionQueues = new HashSet<>(getConsumptionQueues(restTemplate));

        for (String queueName : newHeartbeatQueues) {
            if (!heartbeatQueueNames.contains(queueName)) {
                rabbitQueueService.addNewQueue(queueName, "heartbeatExchange", queueName);
            }
        }
        for (String queueName : newConsumptionQueues) {
            if (!consumptionQueueNames.contains(queueName)) {
                rabbitQueueService.addNewQueue(queueName, "consumptionExchange", queueName);
            }
        }

        this.heartbeatQueueNames = newHeartbeatQueues.stream().toList();
        this.consumptionQueueNames = newConsumptionQueues.stream().toList();
    }

    @Bean
    public List<String> heartbeatQueueNames() {
        return heartbeatQueueNames;
    }

    @Bean
    public List<String> consumptionQueueNames() {
        return consumptionQueueNames;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }


}
