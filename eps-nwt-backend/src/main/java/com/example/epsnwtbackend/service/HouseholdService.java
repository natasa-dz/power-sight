package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.*;
import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.repository.AccessGrantedRepository;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

    @Autowired
    private InfluxService influxService;

    @Autowired
    private AccessGrantedRepository accessGrantedRepository;

    @Cacheable(value = "householdDetails", key = "#id")
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

    // for receipts
    public List<Household> getAll() {
        return householdRepository.findByOwnerIsNotNull();
    }

    @Cacheable(value = "searchNoOwner", key = "{#municipality, #address, #apartmentNumber, #pageable.pageNumber, #pageable.pageSize}")
    public CacheablePage<HouseholdSearchDTO> searchNoOwner(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        Page<HouseholdSearchDTO> households =  householdRepository.findHouseholdsWithoutOwner(municipality, address, apartmentNumber, pageable);
        return new CacheablePage<HouseholdSearchDTO>(new ArrayList<>(households.getContent()), households.getTotalPages(), households.getTotalElements());
    }

    @Cacheable(value = "noOwnerHouseholds", key = "{#pageable.pageNumber, #pageable.pageSize}")
    public Page<HouseholdDto> noOwnerHouseholds(Pageable pageable) {
        Page<HouseholdDto> households =  householdRepository.searchNoOwner(pageable);
        return households;
        //return new CacheablePage<HouseholdDto>(new ArrayList<>(households.getContent()), households.getTotalPages(), households.getTotalElements());
    }

    @Cacheable(value = "ownerHouseholds", key = "{#ownerId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<HouseholdDto> ownerHouseholds(Pageable pageable, Long ownerId){
        Page<HouseholdDto> households = householdRepository.searchOwner(pageable, ownerId);
        return households;

        //return new CacheablePage<HouseholdDto>(new ArrayList<>(households.getContent()), households.getTotalPages(), households.getTotalElements());
    }

    @Cacheable(value = "householdSearch", key = "{#municipality, #address, #apartmentNumber, #pageable.pageNumber, #pageable.pageSize}")
    public CacheablePage<HouseholdSearchDTO> search(String municipality, String address, Integer apartmentNumber, Pageable pageable) {
        Page<HouseholdSearchDTO> households = householdRepository.findAllOnAddress(municipality, address, apartmentNumber, pageable);
        return new CacheablePage<HouseholdSearchDTO>(new ArrayList<>(households.getContent()), households.getTotalPages(), households.getTotalElements());
    }

    public List<AggregatedAvailabilityData> fillMissingData(List<AggregatedAvailabilityData> aggregatedData,
                                                             String aggregationPeriod, String timeRange) {
        List<AggregatedAvailabilityData> filledData = new ArrayList<>(aggregatedData);

        LocalDate[] dateRange = null;
        try {
            dateRange = parseDateRange(timeRange);
        } catch (Exception e) {
        }

        switch (aggregationPeriod) {
            case "hourly":
                if (dateRange != null) {
                    long hoursBack = ChronoUnit.HOURS.between(dateRange[0].atStartOfDay(), dateRange[1].atStartOfDay());
                    for (long i = 0; i <= hoursBack; i++) {
                        String hour = dateRange[0].atTime((int) (i % 24), 0).format(DateTimeFormatter.ofPattern("HH'h'"));
                        if (filledData.stream().noneMatch(data -> data.getName().equals(hour))) {
                            filledData.add(new AggregatedAvailabilityData(hour, 0));
                        }
                    }
                } else {
                    int hoursBack = Integer.parseInt(timeRange);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.HOUR_OF_DAY, -hoursBack - 1);
                    int startHour = cal.get(Calendar.HOUR_OF_DAY);
                    String[] hoursInDay = {"01h", "02h", "03h", "04h", "05h",
                            "06h", "07h", "08h", "09h", "10h", "11h", "12h", "13h",
                            "14h", "15h", "16h", "17h", "18h", "19h", "20h", "21h",
                            "22h", "23h", "00h"};
                    if(aggregatedData.size()!=0) {
                        for (int i = 0; i < hoursBack - aggregatedData.size() + 1; i++) {
                            int hourIndex = (startHour + i + 1) % 24;
                            String hour = hoursInDay[hourIndex];
                            if (filledData.stream().noneMatch(data -> data.getName().equals(hour))) {
                                filledData.add(new AggregatedAvailabilityData(hour, 0));
                            }
                        }
                    } else {
                        for (int i = 0; i < hoursBack; i++) {
                            int hourIndex = (startHour + i + 1) % 24;
                            String hour = hoursInDay[hourIndex];
                            if (filledData.stream().noneMatch(data -> data.getName().equals(hour))) {
                                filledData.add(new AggregatedAvailabilityData(hour, 0));
                            }
                        }
                    }
                }
                break;
            case "daily":
                if (dateRange != null) {
                    long daysBack = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]);
                    for (long i = 0; i <= daysBack; i++) {
                        String day = dateRange[0].plusDays(i).format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
                        if (filledData.stream().noneMatch(data -> data.getName().equals(day))) {
                            filledData.add(new AggregatedAvailabilityData(day, 0));
                        }
                    }
                } else {
                    int daysBack = 6;   //only week has aggregation by days, so today + 6 days back
                    LocalDate startDate = LocalDate.now().minusDays(daysBack);
                    for (long i = 0; i <= daysBack; i++) {
                        String day = startDate.plusDays(i).format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
                        if (filledData.stream().noneMatch(data -> data.getName().equals(day))) {
                            filledData.add(new AggregatedAvailabilityData(day, 0));
                        }
                    }
                }
                break;
            case "weekly":
                if (dateRange != null) {
                    long weeksBack = ChronoUnit.WEEKS.between(dateRange[0], dateRange[1]);
                    for (long i = 0; i <= weeksBack; i++) {
                        String week = "";
                        switch ((int) i + 1) {
                            case 1: week = "1st"; break;
                            case 2: week = "2nd"; break;
                            case 3: week = "3rd"; break;
                            default: week = i + "th"; break;
                        }
                        String finalWeek = week;
                        if (filledData.stream().noneMatch(data -> data.getName().equals(finalWeek))) {
                            filledData.add(new AggregatedAvailabilityData(week, 0));
                        }
                    }
                } else {
                    String[] weeksOfMonth = {"1st", "2nd", "3rd", "4th", "5th"};
                    for (String week : weeksOfMonth) {
                        if (filledData.stream().noneMatch(data -> data.getName().equals(week))) {
                            filledData.add(new AggregatedAvailabilityData(week, 0));
                        }
                    }
                }
                break;
            case "monthly":
                if (dateRange != null) {
                    long monthsBack = ChronoUnit.MONTHS.between(YearMonth.from(dateRange[0]), YearMonth.from(dateRange[1]));
                    for (long i = 0; i <= monthsBack; i++) {
                        String month = dateRange[0].plusMonths(i).format(DateTimeFormatter.ofPattern("MMM"));
                        if (filledData.stream().noneMatch(data -> data.getName().equals(month))) {
                            filledData.add(new AggregatedAvailabilityData(month, 0));
                        }
                    }
                } else {
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
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.ofHours(1)).truncatedTo(ChronoUnit.HOURS)
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByDay(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.ofHours(1)).toLocalDate().atStartOfDay()
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByWeek(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.ofHours(1))
                        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        .truncatedTo(ChronoUnit.DAYS)
        ));
    }

    private Map<LocalDateTime, List<AvailabilityData>> groupByMonth(List<AvailabilityData> data) {
        return data.stream().collect(Collectors.groupingBy(
                d -> LocalDateTime.ofInstant(d.getTimestamp(), ZoneOffset.ofHours(1))
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
            name = periodStart.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
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
        if (timeRange.equalsIgnoreCase("3") || timeRange.equalsIgnoreCase("6") || timeRange.equalsIgnoreCase("12") || timeRange.equalsIgnoreCase("24")) {
            return "hourly";
        } else if (timeRange.equalsIgnoreCase("week")) {
            return "daily";
        } else if (timeRange.equalsIgnoreCase("month")) {
            return "weekly";
        } else if (timeRange.equalsIgnoreCase("3months") || timeRange.equalsIgnoreCase("year")) {
            return "monthly";
        } else {
            LocalDate[] dateRange = parseDateRange(timeRange);
            long daysBetween = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]);
            long weeksBetween = ChronoUnit.WEEKS.between(dateRange[0], dateRange[1]);
            if (daysBetween < 15) {
                return "daily";
            } else if (weeksBetween <= 6) {
                return "weekly";
            } else {
                return "monthly";
            }
        }
    }

    private LocalDate[] parseDateRange(String timeRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        String[] dates = timeRange.split("-");

        try {
            LocalDate startDate = LocalDate.parse(dates[0].trim(), formatter);
            LocalDate endDate = LocalDate.parse(dates[1].trim(), formatter);
            if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must be at least 1 day!");
            }
            return new LocalDate[]{startDate, endDate};
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range format!");
        }
    }

    public List<AggregatedAvailabilityData> getDataForGraph(
            @PathVariable String name, @PathVariable String timeRange) {
        LocalDate[] dateRange = null;
        String duration = null;

        try {
            if (timeRange.contains("-")) {
                dateRange = parseDateRange(timeRange);
            } else {
                duration = parseTimeRange(timeRange);
            }
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }

        List<AvailabilityData> allData;
        if (dateRange != null) {
            allData = influxService.getAvailabilityByDateRange(name, dateRange[0], dateRange[1]);
        } else {
            allData = influxService.getAvailabilityByTimeRange(name, duration);
        }
        String aggregationPeriod = determineAggregationPeriod(timeRange);

        List<AggregatedAvailabilityData> aggregatedData = aggregateData(allData, aggregationPeriod);

        aggregatedData = fillMissingData(aggregatedData, aggregationPeriod, timeRange);

        return aggregatedData;
    }

    public boolean getCurrentStatus(String name) {
        return influxService.getLatestAvailability(name);
    }

    public String parseTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "3" -> "3h";
            case "6" -> "6h";
            case "12" -> "12h";
            case "24" -> "1d";
            case "week" -> "7d";
            case "month" -> "30d";
            case "3months" -> "90d";
            case "year" -> "365d";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time range format!");
        };
    }

    public List<String> getAllSimulatorIds() {
        return householdRepository.findAll().stream()
                .map(household -> household.getId().toString())
                .collect(Collectors.toList());
    }

    @Cacheable(value = "householdAccess", key = "#id")
    public List<HouseholdAccessDTO> getHouseholdsForOwner(Long id) throws NoResourceFoundException {
        List<Household> households = householdRepository.findForOwner(id);
        List<HouseholdAccessDTO> dtos = new ArrayList<>();
        if (!households.isEmpty()) {
            for(Household household : households){
                HouseholdAccessDTO householdAccessDTO = new HouseholdAccessDTO();
                householdAccessDTO.setId(household.getId());
                householdAccessDTO.setFloor(household.getFloor());
                householdAccessDTO.setApartmentNumber(household.getApartmentNumber());
                householdAccessDTO.setSquareFootage(household.getSquareFootage());

                householdAccessDTO.setOwnerId(household.getOwner() == null ? null : household.getOwner().getId());
                householdAccessDTO.setAccessGranted(accessGrantedRepository.findCitizenIdsByHouseholdId(household.getId()));
                householdAccessDTO.setAddress(household.getRealEstate().getAddress());
                householdAccessDTO.setTown(household.getRealEstate().getTown());
                householdAccessDTO.setMunicipality(household.getRealEstate().getMunicipality());
                dtos.add(householdAccessDTO);
            }

            return dtos;
        }
        throw new NoResourceFoundException(HttpMethod.GET, "Household with this id does not exist");
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "householdDetails", key = "#householdId"),
            @CacheEvict(value = "householdAccess", allEntries = true)
    })
    public void allowAccess(Long householdId, List<Long> ids) {
        System.out.println("usaooooo");
        Household household = householdRepository.getReferenceById(householdId);
        List<Long> existingIds = accessGrantedRepository.findCitizenIdsByHouseholdId(householdId);
        List<Long> idsToDelete = existingIds.stream()
                .filter(existingId -> !ids.contains(existingId))
                .toList();
        List<Long> idsToAdd = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        System.out.println("na pola");
        for (Long idToDelete : idsToDelete) {
            accessGrantedRepository.deleteByHouseholdIdAndCitizenId(householdId, idToDelete);
        }
        System.out.println("jos ovde");
        for(Long idToAdd : idsToAdd){
            System.out.println(idToAdd);
        }
        System.out.println("jos delete");
        for(Long idToAdd : idsToDelete){
            System.out.println(idToAdd);
        }
        for (Long id : idsToAdd) {
            AccessGranted accessGranted = new AccessGranted();
            accessGranted.setHousehold(household);
            accessGranted.setCitizenId(id);
            accessGrantedRepository.save(accessGranted);
        }
    }
}
