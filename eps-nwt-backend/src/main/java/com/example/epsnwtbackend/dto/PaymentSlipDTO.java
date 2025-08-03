package com.example.epsnwtbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSlipDTO {
    private String customerName;
    private String customerAddress;
    private String recipientName;
    private String recipientAccount;
    private Double amount;
    private String purpose;
    private Integer model;
    private String referenceNumber;
}
