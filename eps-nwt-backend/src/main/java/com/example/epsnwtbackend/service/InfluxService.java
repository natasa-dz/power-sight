package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.AvailabilityData;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InfluxService {

    @Value("${influxdb.bucket.heartbeat}")
    private String heartbeatBucket;

    @Value("${influxdb.bucket.consumption}")
    private String consumptionBucket;

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

    public boolean getLatestAvailability(String name) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: -1y) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> last()",
                this.heartbeatBucket, name);
        List<AvailabilityData> data = this.queryAvailability(fluxQuery);
        if(data.isEmpty()) return false;
        AvailabilityData availabilityData = data.getFirst();
        return availabilityData.isOnline();
    }

    public List<AvailabilityData> getAvailabilityByTimeRange(String measurementName, String duration) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: -%s, stop: now()) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\")" +
                        "|> yield(name: \"all\")",
                this.heartbeatBucket, duration.toString(), measurementName);
        return this.queryAvailability(fluxQuery);
    }

    public List<AvailabilityData> getAvailabilityByDateRange(String measurementName, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        String start = startDate.atStartOfDay().format(formatter);
        String end = endDate.plusDays(1).atStartOfDay().format(formatter);
        String fluxQuery = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\")" +
                        "|> yield(name: \"all\")",
                this.heartbeatBucket, start, end, measurementName);
        return this.queryAvailability(fluxQuery);
    }

    private List<AvailabilityData> queryAvailability(String fluxQuery) {
        List<AvailabilityData> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClientHeartbeat.getQueryApi();
        List<FluxTable> tables;
        try {
            tables = queryApi.query(fluxQuery);
        } catch (Exception e) {
            //if something goes wrong just return an empty list
            return new ArrayList<>();
        }
        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                boolean isOnline = ((Double) record.getValue()).intValue() == 1;
                result.add(new AvailabilityData(record.getTime(), isOnline));
            }
        }
        return result;
    }

    public /*List<MeterConsumptionData>*/ void getConsumptionForCityByDateRange(String city, String measurementName, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        String start = startDate.atStartOfDay().format(formatter);
        String end = endDate.plusDays(1).atStartOfDay().format(formatter);

        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> filter(fn: (r) => r[\"city\"] == \"%s\") " +
                        "|> group(columns: [\"meter_id\"]) " +
                        "|> sum(column: \"_value\") " +
                        "|> yield(name: \"total_consumption\")",
                this.consumptionBucket, start, end, measurementName, city);

        //return this.queryMeterConsumption(fluxQuery);
    }
}
