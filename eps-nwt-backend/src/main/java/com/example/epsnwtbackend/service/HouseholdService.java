package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.AggregatedAvailabilityData;
import com.example.epsnwtbackend.dto.AvailabilityData;
import com.example.epsnwtbackend.dto.HouseholdSearchDTO;
import com.example.epsnwtbackend.dto.ViewHouseholdDTO;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HouseholdService {

    @Autowired
    private HouseholdRepository householdRepository;

    public ViewHouseholdDTO getHousehold(Long id) throws NoResourceFoundException {
        Optional<Household> reference = householdRepository.findById(id);
        if (reference.isPresent()) {
            Household household = reference.get();
            ViewHouseholdDTO viewHouseholdDTO = new ViewHouseholdDTO();
            viewHouseholdDTO.setId(household.getId());
            viewHouseholdDTO.setFloor(household.getFloor());
            viewHouseholdDTO.setApartmentNumber(household.getApartmentNumber());
            viewHouseholdDTO.setSquareFootage(household.getSquareFootage());

            viewHouseholdDTO.setOwnerId(household.getOwner() == null ? null : household.getOwner().getId());

            viewHouseholdDTO.setAddress(household.getRealEstate().getAddress());
            viewHouseholdDTO.setTown(household.getRealEstate().getTown());
            viewHouseholdDTO.setMunicipality(household.getRealEstate().getMunicipality());
            return viewHouseholdDTO;
        }
        throw new NoResourceFoundException(HttpMethod.GET, "Household with this id does not exist");
    }

    public Page<HouseholdSearchDTO> searchNoOwner(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        return householdRepository.findHouseholdsWithoutOwner(municipality, address, apartmentNumber, pageable);
    }

    public Page<HouseholdSearchDTO> search(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        return householdRepository.findAllOnAddress(municipality, address, apartmentNumber, pageable);
    }

    public List<AggregatedAvailabilityData> fillMissingData(List<AggregatedAvailabilityData> aggregatedData,
                                                            String aggregationPeriod, String timeRange) {
        List<AggregatedAvailabilityData> filledData = new ArrayList<>(aggregatedData);
        switch (aggregationPeriod) {
            case "hourly":
                String[] hoursInDay = {"00h", "01h", "02h", "03h", "04h", "05h",
                        "06h", "07h", "08h", "09h", "10h", "11h", "12h", "13h",
                        "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h",
                        "22h", "23h"};
                int hoursBack = Integer.parseInt(timeRange);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY, -hoursBack-1);
                int startHour = cal.get(Calendar.HOUR_OF_DAY);
                for (int i = 0; i < hoursBack; i++) {
                    int hourIndex = (startHour + i) % 24;
                    String hour = hoursInDay[hourIndex];
                    if (filledData.stream().noneMatch(data -> data.getName().equals(hour))) {
                        filledData.add(new AggregatedAvailabilityData(hour, 0));
                    }
                }
                break;
            case "daily":
                String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                for (String day : daysOfWeek) {
                    if (filledData.stream().noneMatch(data -> data.getName().equals(day))) {
                        filledData.add(new AggregatedAvailabilityData(day, 0));
                    }
                }
                break;
            case "weekly":
                String[] weeksOfMonth = {"1st", "2nd", "3rd", "4th", "5th"};
                for (String week : weeksOfMonth) {
                    if (filledData.stream().noneMatch(data -> data.getName().equals(week))) {
                        filledData.add(new AggregatedAvailabilityData(week, 0));
                    }
                }
                break;
            case "monthly":
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                        "Aug", "Sep", "Oct", "Nov", "Dec"};
                int monthsBack = timeRange.equals("3months") ? 3 : 12;
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                for (int i = 0; i < monthsBack; i++) {
                    int monthIndex = (currentMonth - i + 12) % 12;
                    String month = months[monthIndex];
                    if (filledData.stream().noneMatch(data -> data.getName().equals(month))) {
                        filledData.add(new AggregatedAvailabilityData(month, 0));
                    }
                }
                break;
        }
        return filledData;
    }

    public List<AggregatedAvailabilityData> aggregateData(List<AvailabilityData> allData, String aggregationPeriod) {
        Map<LocalDateTime, List<AvailabilityData>> groupedData;

        switch (aggregationPeriod) {
            case "hourly":
                groupedData = groupByHour(allData);
                break;
            case "daily":
                groupedData = groupByDay(allData);
                break;
            case "weekly":
                groupedData = groupByWeek(allData);
                break;
            case "monthly":
                groupedData = groupByMonth(allData);
                break;
            default:
                throw new IllegalArgumentException("Invalid aggregation period");
        }

        return groupedData.entrySet().stream()
                .map(entry -> aggregateAvailability(entry.getKey(), entry.getValue(), aggregationPeriod))
                .collect(Collectors.toList());
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByHour(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS)
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByDay(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.UTC).toLocalDate().atStartOfDay()
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByWeek(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.UTC)
                        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        .truncatedTo(ChronoUnit.DAYS)
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByMonth(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.UTC)
                        .withDayOfMonth(1)
                        .truncatedTo(ChronoUnit.DAYS)
        ));
    }

    private AggregatedAvailabilityData aggregateAvailability(LocalDateTime time, List<AvailabilityData> data,
                                                             String aggregationPeriod) {
        long onlineCount = data.stream().filter(AvailabilityData::isOnline).count();
        long totalCount = 0;
        switch (aggregationPeriod) {
            case "hourly":
                totalCount = 60 * 60 / 15;
                break;
            case "daily":
                totalCount = 60 * 60 * 24 / 15;
                break;
            case "weekly":
                totalCount = 60 * 60 * 24 * 7 / 15;
                break;
            case "monthly":
                totalCount = 60 * 60 * 24 * 30 / 15;
                break;
            default:
                throw new IllegalArgumentException("Invalid aggregation period");
        }

        double availabilityPercentage = (totalCount > 0) ? ((double) onlineCount / totalCount) : 0;

        return new AggregatedAvailabilityData(getName(aggregationPeriod, time), availabilityPercentage);
    }

    private String getName(String aggregationPeriod, LocalDateTime periodStart) {
        String name;
        if ("hourly".equals(aggregationPeriod)) {
            name = periodStart.format(DateTimeFormatter.ofPattern("HH'h'"));
        } else if ("daily".equals(aggregationPeriod)) {
            name = periodStart.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        } else if ("weekly".equals(aggregationPeriod)) {
            int weekOfYear = periodStart.get(WeekFields.ISO.weekOfMonth());
            switch (weekOfYear) {
                case 1: name = "1st"; break;
                case 2: name = "2nd"; break;
                case 3: name = "3rd"; break;
                default: name = weekOfYear + "th"; break;
            }
        } else { // monthly
            name = periodStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        }
        return name;
    }

    public String determineAggregationPeriod(String timeRange) {
        if (timeRange.equalsIgnoreCase("3") || timeRange.equalsIgnoreCase("6") || timeRange.equalsIgnoreCase("12")) {
            return "hourly";
        } else if (timeRange.equalsIgnoreCase("week")) {
            return "daily";
        } else if (timeRange.equalsIgnoreCase("month")) {
            return "weekly";
        }
        return "monthly";
    }
}
