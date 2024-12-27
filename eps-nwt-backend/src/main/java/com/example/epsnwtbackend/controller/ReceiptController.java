package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {
    @Autowired private ReceiptService receiptService;

    @PostMapping("/create/{month}/{year}")
    public ResponseEntity<Void> createForMonth(@PathVariable String month, @PathVariable int year) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        if (Arrays.stream(months).noneMatch(m -> m.equalsIgnoreCase(month))) {
            return ResponseEntity.badRequest().build();
        }
        try {
            receiptService.createReceipts(month, year);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
