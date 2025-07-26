package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.model.PriceList;
import com.example.epsnwtbackend.model.Receipt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {
    private Long id;
    private PriceList priceList;
    private Long householdId;
    private String householdAddress;
    private Integer householdApartmentNumber;
    private Long ownerId;
    private String ownerUsername;
    private Double price;
    private Double greenZoneConsumption;
    private Double blueZoneConsumption;
    private Double redZoneConsumption;
    private boolean isPaid;
    private Date paymentDate;
    private String path;
    private String month;
    private int year;

    public static ReceiptDTO toDTO(Receipt receipt) {
        if (receipt == null) {
            return null;
        }

        Household household = receipt.getHousehold();
        return new ReceiptDTO(
                receipt.getId(),
                receipt.getPriceList(),
                household != null ? household.getId() : null,
                household != null ? household.getRealEstate().getAddress() : null,
                household != null ? household.getApartmentNumber() : null,
                household != null && household.getOwner() != null ? household.getOwner().getId() : null,
                household != null && household.getOwner() != null ? household.getOwner().getUsername() : null,
                receipt.getPrice(),
                receipt.getGreenZoneConsumption(),
                receipt.getBlueZoneConsumption(),
                receipt.getRedZoneConsumption(),
                receipt.isPaid(),
                receipt.getPaymentDate(),
                receipt.getPath(),
                receipt.getMonth(),
                receipt.getYear()
        );
    }

}
