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
public class PriceList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Date startDate;

    @Column
    private Date endDate;

    @Column
    private Double greenZone;

    @Column
    private Double blueZone;

    @Column
    private Double redZone;

    @Column
    private Double basePrice;

    @Column
    private Double pdvPercentage;
}
