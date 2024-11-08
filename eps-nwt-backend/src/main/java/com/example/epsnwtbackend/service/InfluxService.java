package com.example.epsnwtbackend.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Service
public class InfluxService {

    private final InfluxDBClient influxDbClientHeartbeat;
    private final InfluxDBClient influxDbClientConsumption;

    public InfluxService(InfluxDBClient influxDbClientHeartbeat, InfluxDBClient influxDbClientConsumption) {
        this.influxDbClientHeartbeat = influxDbClientHeartbeat;
        this.influxDbClientConsumption = influxDbClientConsumption;
    }

    public void saveHeartbeat(String name, float value, Date timestamp, Map<String, String> tags) {
        WriteApiBlocking writeApi = this.influxDbClientHeartbeat.getWriteApiBlocking();

        Point point = Point.measurement(name)
                .addTags(tags)
                .addField("value", value)
                .time(timestamp.toInstant(), WritePrecision.MS);
        writeApi.writePoint(point);
    }


    public void saveConsumption(String name, float value, Date timestamp, Map<String, String> tags) {
        WriteApiBlocking writeApi = this.influxDbClientConsumption.getWriteApiBlocking();

        ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(ZoneOffset.UTC).plusHours(1);
        Instant adjustedTimestamp = zonedDateTime.toInstant();

        Point point = Point.measurement(name)
                .addTags(tags)
                .addField("consumption_value", value)
                .time(adjustedTimestamp, WritePrecision.MS);
        writeApi.writePoint(point);
    }

}
