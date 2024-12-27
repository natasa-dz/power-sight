package com.example.epsnwtbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "households", indexes = {
        @Index(name = "idx_household_owner", columnList = "user_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer floor;

    @Column
    private Float squareFootage;

    @Column
    private Integer apartmentNumber;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Meter meters;

    @ManyToOne
    @JoinColumn(name = "real_estate_id", nullable = false)
    private RealEstate realEstate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User owner;

    @Column
    private List<Long> accessGranted;
}
