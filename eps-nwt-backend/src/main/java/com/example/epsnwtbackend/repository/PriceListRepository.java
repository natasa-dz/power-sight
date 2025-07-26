package com.example.epsnwtbackend.repository;

import com.example.epsnwtbackend.model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    PriceList findPriceListByStartDateLessThanEqualAndEndDateGreaterThanEqualOrEndDateIsNull(Date date1, Date date2);

}
