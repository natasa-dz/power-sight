package com.example.epsnwtbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "price_list_id", nullable = false)
    private PriceList priceList;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column
    private Double price;

    @Column
    private Double greenZoneConsumption;

    @Column
    private Double blueZoneConsumption;

    @Column
    private Double redZoneConsumption;

    @Column
    private boolean isPaid;

    @Column
    private Date paymentDate;

    @Column
    private String path;

    @Column
    private String month;

    @Column
    private int year;
}
