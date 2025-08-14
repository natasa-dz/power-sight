package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.PriceList;
import com.example.epsnwtbackend.repository.PriceListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class PriceListService {
    @Autowired private PriceListRepository priceListRepository;

    @CacheEvict(value = "allPriceLists", allEntries = true)
    public void save(PriceList priceList) {
        PriceList newPriceList = priceListRepository.save(priceList);
        if(priceListRepository.findAll().size() != 1) {
            PriceList current = priceListRepository.findPriceListByStartDateLessThanEqualAndEndDateGreaterThanEqualOrEndDateIsNull(new Date(), new Date());
            LocalDate today = LocalDate.now();
            LocalDate lastDayOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
            Date lastDate = java.sql.Date.valueOf(lastDayOfCurrentMonth);
            current.setEndDate(lastDate);
            priceListRepository.save(current);
        }
    }

    @Cacheable(value = "allPriceLists")
    public List<PriceList> findAll() {
        return priceListRepository.findAll();
    }

    @Cacheable(value = "priceListById", key = "#id")
    public PriceList findById(Long id) {
        return priceListRepository.getReferenceById(id);
    }
    @Cacheable(value = "priceListForDate", key = "#date")
    public PriceList findForDate(Date date) {
        return priceListRepository.findPriceListByStartDateLessThanEqualAndEndDateGreaterThanEqualOrEndDateIsNull(date, date);
    }
}
