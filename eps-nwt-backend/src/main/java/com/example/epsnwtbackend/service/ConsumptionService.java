package com.example.epsnwtbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConsumptionService {

    @Autowired
    private InfluxService influxService;

    public String parseTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "1" -> "1h";
            case "6" -> "6h";
            case "12" -> "12h";
            case "24" -> "24h";
            case "week" -> "7d";
            case "month" -> "30d";
            case "3months" -> "90d";
            case "year" -> "365d";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time range format!");
        };
    }

    public String determineAggregationPeriod(String timeRange) {
        if (timeRange.equalsIgnoreCase("1") || timeRange.equalsIgnoreCase("6") || timeRange.equalsIgnoreCase("12") || timeRange.equalsIgnoreCase("24")) {
            return "hourly";
        } else if (timeRange.equalsIgnoreCase("week")) {
            return "daily";
        } else if (timeRange.equalsIgnoreCase("month")) {
            return "weekly";
        } else if (timeRange.equalsIgnoreCase("3months") || timeRange.equalsIgnoreCase("year")) {
            return "monthly";
        } else {
            LocalDateTime[] dateRange = parseDateRange(timeRange);
            long daysBetween = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]);
            long weeksBetween = ChronoUnit.WEEKS.between(dateRange[0], dateRange[1]);
            if (daysBetween < 2) {
                return "hourly";
            }
            else if (daysBetween < 15) {
                return "daily";
            } else if (weeksBetween <= 6) {
                return "weekly";
            } else {
                return "monthly";
            }
        }
    }

    public LocalDateTime[] parseDateRange(String timeRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
        String[] dates = timeRange.replace("%20", " ").split("-");

        try {
            LocalDateTime startDate = LocalDateTime.parse(dates[0].trim(), formatter);
            LocalDateTime endDate = LocalDateTime.parse(dates[1].trim(), formatter);
            if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must be at least 1 day!");
            }
            if (ChronoUnit.DAYS.between(startDate, endDate) > 365) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must not exceed 1 year!");
            }
            return new LocalDateTime[]{startDate, endDate};
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range format!");
        }
    }

    public Map<String, Double>getGraphConsumptionData(String city, String timeRange){
        LocalDateTime[] dateRange = null;
        String duration = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = parseDateRange(timeRange);
            } else {
                duration = parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return null;
        }

        Map<String, Double> result = new HashMap<>();

        String period = determineAggregationPeriod(timeRange);
        switch (period) {
            case "hourly":
                if (dateRange != null) { // 09.09.2031. - 10.09.2031.
                    int hoursBetween = (int) ChronoUnit.HOURS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < hoursBetween; i++) {
                        Double consumption = influxService.getConsumptionForCityByDateRange(city, dateRange[0].plusHours(i), dateRange[0].minusDays(1).plusHours(i+1));
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", i) + "h", consumption);
                    }
                } else {
                    int hoursBack = Integer.parseInt(duration.replace("h", ""));
                    for (int i = 0; i < hoursBack; i++) {
                        Double consumption = influxService.getConsumptionForCityByTimeRangeForGraph(city, "-"+i+1+"h", "-"+i+"h");
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", LocalDateTime.now().minusHours(i).getHour()) + "h", consumption);
                    }
                }
                break;
            case "daily":
                if (dateRange != null) {
                    int daysBetween = (int) ChronoUnit.DAYS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < daysBetween; i++) {
                        Double consumption = influxService.getConsumptionForCityByDateRange(city, dateRange[0].plusDays(i), dateRange[0].minusDays(1).plusDays(i+1));
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", dateRange[0].plusDays(i).getDayOfMonth())+"."+
                                String.format("%02d", dateRange[0].plusDays(i).getMonthValue())+"."+
                                dateRange[0].plusDays(i).getYear()+".", consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    for (int i = 0; i < daysBack; i++) {
                        Double consumption = influxService.getConsumptionForCityByTimeRangeForGraph(city, "-"+i+1+"d", "-"+i+"d");
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", LocalDateTime.now().minusDays(i).getDayOfMonth())+"."+
                                String.format("%02d", LocalDateTime.now().minusDays(i).getMonthValue())+"."+
                                LocalDateTime.now().minusDays(i).getYear()+".", consumption);
                    }
                }
                break;
            case "weekly":
                if (dateRange != null) {
                    int weeksBetween = (int) ChronoUnit.WEEKS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < weeksBetween; i++) {
                        Double consumption = influxService.getConsumptionForCityByDateRange(city, dateRange[0].plusWeeks(i), dateRange[0].minusDays(1).plusWeeks(i+1));
                        if (consumption == null) consumption = 0.0;
                        if (i+1 == 1) result.put(i+1+"st", consumption);
                        else if (i+1 == 2) result.put(i+1+"nd", consumption);
                        else if (i+1 == 3) result.put(i+1+"rd", consumption);
                        else result.put(i+1+"th", consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    int weeksBack = daysBack / 7;
                    if (daysBack % 7 != 0) weeksBack += 1;
                    for (int i = 0; i < weeksBack; i++) {
                        Double consumption = influxService.getConsumptionForCityByTimeRangeForGraph(city, "-"+i+1+"w", "-"+i+"w");
                        if (consumption == null) consumption = 0.0;
                        if (i+1 == 1) result.put(i+1+"st", consumption);
                        else if (i+1 == 2) result.put(i+1+"nd", consumption);
                        else if (i+1 == 3) result.put(i+1+"rd", consumption);
                        else result.put(i+1+"th", consumption);
                    }
                }
                break;
            case "monthly":
                if (dateRange != null) {
                    int monthsBetween = (int) Math.ceil(ChronoUnit.MONTHS.between(dateRange[0], dateRange[1]))+1;
                    for (int i = 0; i < monthsBetween; i++) {
                        Double consumption = influxService.getConsumptionForCityByDateRange(city, dateRange[0].plusMonths(i), dateRange[0].minusDays(1).plusMonths(i+1));
                        if (consumption == null) consumption = 0.0;
                        result.put(dateRange[0].plusMonths(i).getMonth().name(), consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    int monthsBack = daysBack / 30;
                    if (daysBack % 30 != 0) monthsBack += 1;
                    for (int i = 0; i < monthsBack; i++) {
                        Double consumption = influxService.getConsumptionForCityByTimeRangeForGraph(city, "-"+i+1+"mo", "-"+i+"mo");
                        if (consumption == null) consumption = 0.0;
                        result.put(LocalDateTime.now().minusMonths(i).getMonth().name(), consumption);
                    }
                }
                break;
        }

        return result;
    }

    public Map<String, Double> getHouseholdConsumptionGraph(Long householdId, String timeRange) {

        LocalDateTime[] dateRange = null;
        String duration = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = parseDateRange(timeRange);
            } else {
                duration = parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return null;
        }

        Map<String, Double> result = new HashMap<>();

        String period = determineAggregationPeriod(timeRange);
        switch (period) {
            case "hourly":
                if (dateRange != null) { // 09.09.2031. - 10.09.2031.
                    int hoursBetween = (int) ChronoUnit.HOURS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < hoursBetween; i++) {
                        Double consumption = influxService.getHouseholdConsumptionByDateRange(
                                householdId,
                                dateRange[0].plusHours(i),
                                dateRange[0].plusHours(i+1)
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", i) + "h", consumption);
                    }
                } else {
                    int hoursBack = Integer.parseInt(duration.replace("h", ""));
                    for (int i = 0; i < hoursBack; i++) {
                        Double consumption = influxService.getHouseholdConsumptionForGraph(
                                householdId,
                                "-" + (i+1) + "h",
                                "-" + i + "h"
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", LocalDateTime.now().minusHours(i).getHour()) + "h", consumption);
                    }
                }
                break;

            case "daily":
                if (dateRange != null) {
                    int daysBetween = (int) ChronoUnit.DAYS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < daysBetween; i++) {
                        Double consumption = influxService.getHouseholdConsumptionByDateRange(
                                householdId,
                                dateRange[0].plusDays(i),
                                dateRange[0].plusDays(i+1)
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", dateRange[0].plusDays(i).getDayOfMonth())+"."+
                                String.format("%02d", dateRange[0].plusDays(i).getMonthValue())+"."+
                                dateRange[0].plusDays(i).getYear()+".", consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    for (int i = 0; i < daysBack; i++) {
                        Double consumption = influxService.getHouseholdConsumptionForGraph(
                                householdId,
                                "-" + (i+1) + "d",
                                "-" + i + "d"
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(String.format("%02d", LocalDateTime.now().minusDays(i).getDayOfMonth())+"."+
                                String.format("%02d", LocalDateTime.now().minusDays(i).getMonthValue())+"."+
                                LocalDateTime.now().minusDays(i).getYear()+".", consumption);
                    }
                }
                break;

            case "weekly":
                if (dateRange != null) {
                    int weeksBetween = (int) ChronoUnit.WEEKS.between(dateRange[0], dateRange[1]);
                    for (int i = 0; i < weeksBetween; i++) {
                        Double consumption = influxService.getHouseholdConsumptionByDateRange(
                                householdId,
                                dateRange[0].plusWeeks(i),
                                dateRange[0].plusWeeks(i+1)
                        );
                        if (consumption == null) consumption = 0.0;
                        if (i+1 == 1) result.put((i+1)+"st", consumption);
                        else if (i+1 == 2) result.put((i+1)+"nd", consumption);
                        else if (i+1 == 3) result.put((i+1)+"rd", consumption);
                        else result.put((i+1)+"th", consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    int weeksBack = daysBack / 7;
                    if (daysBack % 7 != 0) weeksBack += 1;
                    for (int i = 0; i < weeksBack; i++) {
                        Double consumption = influxService.getHouseholdConsumptionForGraph(
                                householdId,
                                "-" + (i+1) + "w",
                                "-" + i + "w"
                        );
                        if (consumption == null) consumption = 0.0;
                        if (i+1 == 1) result.put((i+1)+"st", consumption);
                        else if (i+1 == 2) result.put((i+1)+"nd", consumption);
                        else if (i+1 == 3) result.put((i+1)+"rd", consumption);
                        else result.put((i+1)+"th", consumption);
                    }
                }
                break;

            case "monthly":
                if (dateRange != null) {
                    int monthsBetween = (int) Math.ceil(ChronoUnit.MONTHS.between(dateRange[0], dateRange[1]))+1;
                    for (int i = 0; i < monthsBetween; i++) {
                        Double consumption = influxService.getHouseholdConsumptionByDateRange(
                                householdId,
                                dateRange[0].plusMonths(i),
                                dateRange[0].plusMonths(i+1)
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(dateRange[0].plusMonths(i).getMonth().name(), consumption);
                    }
                } else {
                    int daysBack = Integer.parseInt(duration.replace("d", ""));
                    int monthsBack = daysBack / 30;
                    if (daysBack % 30 != 0) monthsBack += 1;
                    for (int i = 0; i < monthsBack; i++) {
                        Double consumption = influxService.getHouseholdConsumptionForGraph(
                                householdId,
                                "-" + (i+1) + "mo",
                                "-" + i + "mo"
                        );
                        if (consumption == null) consumption = 0.0;
                        result.put(LocalDateTime.now().minusMonths(i).getMonth().name(), consumption);
                    }
                }
                break;
        }

        return result;
    }

}




