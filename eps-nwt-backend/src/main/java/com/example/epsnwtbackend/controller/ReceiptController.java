package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.PaymentDTO;
import com.example.epsnwtbackend.dto.PaymentSlipDTO;
import com.example.epsnwtbackend.dto.ReceiptDTO;
import com.example.epsnwtbackend.model.Receipt;
import com.example.epsnwtbackend.service.ReceiptService;
import com.example.epsnwtbackend.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {
    @Autowired private ReceiptService receiptService;
    @Autowired private TokenUtils tokenService;

    @PostMapping("/create/{month}/{year}")
    public ResponseEntity<String> createForMonth(@PathVariable String month, @PathVariable int year) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        if (Arrays.stream(months).noneMatch(m -> m.equalsIgnoreCase(month))) {
            return ResponseEntity.badRequest().body("Month is not in correct format!");
        }
        try {
            receiptService.createReceipts(month, year);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Success");
    }

    @GetMapping("/all-for-household/{householdId}")
    public ResponseEntity<List<ReceiptDTO>> allReceiptsForHousehold(@PathVariable Long householdId){
        try {
            List<ReceiptDTO> receipts = receiptService.getAllReceiptsForHousehold(householdId);
            return ResponseEntity.ok().body(receipts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    @GetMapping("/all-for-owner/{ownerId}")
    public ResponseEntity<List<ReceiptDTO>> allReceiptsForOwner(@PathVariable Long ownerId){
        try {
            List<ReceiptDTO> receipts = receiptService.getAllReceiptsForOwner(ownerId);
            return ResponseEntity.ok().body(receipts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    @GetMapping("/by-id/{receiptId}")
    public ResponseEntity<ReceiptDTO> getReceiptById(@PathVariable Long receiptId){
        System.out.println("POGODIOOO: "+ receiptId);
        try {
            return ResponseEntity.ok().body(receiptService.getReceipt(receiptId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PutMapping("/pay/{receiptId}")
    public ResponseEntity<String> payReceipt(@PathVariable Long receiptId,
                                             @RequestBody PaymentDTO paymentDTO){
        System.out.println("POGODIOOO: "+ paymentDTO.getUsername());
        try {
            receiptService.payment(receiptId, paymentDTO.getUsername());
            return ResponseEntity.ok().body("Success");
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("Receipt not found")){
                return ResponseEntity.notFound().build();
            }
            else{
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
    }
}
