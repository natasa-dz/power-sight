package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.AvailabilityData;
import com.example.epsnwtbackend.dto.ConsumptionData;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InfluxService {

    @Value("${influxdb.bucket.heartbeat}")
    private String heartbeatBucket;

    @Value("${influxdb.bucket.consumption}")
    private String consumptionBucket;

    private final InfluxDBClient influxDbClientHeartbeat;
    private final InfluxDBClient influxDbClientConsumption;

    @Autowired
    private RealEstateRequestService realEstateRequestService;

    private Map<String, List<String>> citiesAndMunicipalities;

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

    public Double getConsumptionForCityByTimeRange(String city, String duration) {
        List<String> municipalities = citiesAndMunicipalities.get(city);

        String municipalityFilter = municipalities.stream()
                .map(municipality -> String.format("r[\"Municipality\"] == \"%s\"", municipality))
                .collect(Collectors.joining(" or "));
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: -%s, stop: now()) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => %s )" +
                        "|> filter(fn: (r) => r[\"_field\"] == \"consumption_value\") " +
                        "|> group(columns: [\"simulator_id\"]) " +
                        "|> sum()" +
                        "|> yield(name: \"total_consumption\")",
                this.consumptionBucket, duration, "simulators", municipalityFilter);

        return this.queryCityConsumption(fluxQuery);
    }

    public Double getConsumptionForCityByTimeRangeForGraph(String city, String start, String stop) {
        List<String> municipalities = citiesAndMunicipalities.get(city);

        String municipalityFilter = municipalities.stream()
                .map(municipality -> String.format("r[\"Municipality\"] == \"%s\"", municipality))
                .collect(Collectors.joining(" or "));
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => %s )" +
                        "|> filter(fn: (r) => r[\"_field\"] == \"consumption_value\") " +
                        "|> group(columns: [\"simulator_id\"]) " +
                        "|> sum()" +
                        "|> yield(name: \"total_consumption\")",
                this.consumptionBucket, start, stop, "simulators", municipalityFilter);

        return this.queryCityConsumption(fluxQuery);
    }

    public Double getConsumptionForCityByDateRange(String city, LocalDateTime startDate, LocalDateTime endDate) {
        List<String> municipalities = citiesAndMunicipalities.get(city);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        String start = startDate.format(formatter);
        String end = endDate.plusDays(1).format(formatter);

        /*
        * from(bucket: "consumptions")
          |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
          |> filter(fn: (r) => r["_measurement"] == "simulators")
          |> filter(fn: (r) => r["Municipality"] == "novisad")
          |> filter(fn: (r) => r["_field"] == "consumption_value")
          |> group(columns: ["simulator_id"])
          |> sum()
          |> yield(name: "total_consumption_per_simulator")
        * */

        String municipalityFilter = municipalities.stream()
                .map(municipality -> String.format("r[\"Municipality\"] == \"%s\"", municipality.replace(" ", "").trim().toLowerCase()))
                .collect(Collectors.joining(" or "));
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") " +
                        "|> filter(fn: (r) => %s )" +
                        "|> filter(fn: (r) => r[\"_field\"] == \"consumption_value\") " +
                        "|> group(columns: [\"id\"]) " +
                        "|> sum()" +
                        "|> yield(name: \"total_consumption\")",
                this.consumptionBucket, start, end, "simulators", municipalityFilter);


        return this.queryCityConsumption(fluxQuery);
    }

    private Double queryCityConsumption(String fluxQuery) {
        List<ConsumptionData> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClientConsumption.getQueryApi();
        List<FluxTable> tables;
        try {
            tables = queryApi.query(fluxQuery);
        } catch (Exception e) {
            e.printStackTrace();
            //if something goes wrong just return an empty list
            return null;
        }
        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                Double consumption = (Double) record.getValue();
                result.add(new ConsumptionData(record.getTime(), consumption));
            }
        }
        if (!result.isEmpty()){
            return result.get(0).getConsumption();
        } else{
            return null;
        }

    }

    public List<String> getMunicipalitiesFromInflux() {
        citiesAndMunicipalities = realEstateRequestService.getCitiesWithMunicipalities();
        String fluxQuery = String.format(
                "from(bucket:\"%s\") " +
                        "|> range(start: 1970-01-01T00:00:00Z, stop: 2099-12-31T00:00:00Z) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"simulators\") " +
                        "|> keep(columns: [\"Municipality\"]) " +
                        "|> distinct(column: \"Municipality\") " +
                        "|> yield(name: \"unique_municipalities\") ",
                this.consumptionBucket);
        return this.queryMunicipalities(fluxQuery);
    }

    private List<String> queryMunicipalities(String fluxQuery) {
        List<String> result = new ArrayList<>();
        QueryApi queryApi = this.influxDbClientConsumption.getQueryApi();
        List<FluxTable> tables;
        try {
            tables = queryApi.query(fluxQuery);
        } catch (Exception e) {
            e.printStackTrace();
            //if something goes wrong just return an empty list
            return new ArrayList<>();
        }
        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                String municipality = (String) record.getValue();
                result.add(municipality);
            }
        }
        return result;
    }
}
