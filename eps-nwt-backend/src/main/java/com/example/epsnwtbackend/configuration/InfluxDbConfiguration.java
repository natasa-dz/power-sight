package com.example.epsnwtbackend.configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class InfluxDbConfiguration {
    private final String url;
    private final String token;
    private final String organization;
    private final String bucketHeartbeat;
    private final String bucketConsumption;



    public InfluxDbConfiguration(Environment env) {
        this.url = String.format("http://%s:%s", env.getProperty("influxdb.host"),
                env.getProperty("influxdb.port"));
        this.token = env.getProperty("influxdb.token");
        this.organization = env.getProperty("influxdb.organization");
        this.bucketHeartbeat = env.getProperty("influxdb.bucket.heartbeat");
        this.bucketConsumption = env.getProperty("influxdb.bucket.consumption");


    }

    @Bean
    public InfluxDBClient influxDbClientHeartbeat() {
        return InfluxDBClientFactory.create(this.url, this.token.toCharArray(),
                this.organization, this.bucketHeartbeat);
    }

    @Bean
    public InfluxDBClient influxDbClientConsumption() {
        return InfluxDBClientFactory.create(this.url, this.token.toCharArray(),
                this.organization, this.bucketConsumption);
    }
}
