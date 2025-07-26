package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.model.PriceList;
import com.example.epsnwtbackend.service.PriceListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/price-list")
public class PriceListController {

    @Autowired private PriceListService priceListService;

    @PostMapping("/create")
    public ResponseEntity<Void> save(@RequestBody PriceList priceList) {
        LocalDate today = LocalDate.now();
        LocalDate firstOfNextMonth = today.plusMonths(1).withDayOfMonth(1);
        Date startDate = java.sql.Date.valueOf(firstOfNextMonth);
        priceList.setStartDate(startDate);
        priceListService.save(priceList);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find-all")
    public List<PriceList> findAll() {
        return priceListService.findAll();
    }

    @GetMapping("/find-by-id/{id}")
    public PriceList findById(@PathVariable Long id) {
        return priceListService.findById(id);
    }

    @GetMapping("/find-for-date/{date}")
    public PriceList findForDate(@PathVariable Date date) {
        return priceListService.findForDate(date);
    }
}
