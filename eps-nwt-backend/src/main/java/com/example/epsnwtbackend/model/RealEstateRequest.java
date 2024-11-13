package com.example.epsnwtbackend.model;

import com.example.epsnwtbackend.enums.RealEstateRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "real_estate_requests")
public class RealEstateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*@ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User owner;*/
    @Column
    private Long owner;

    @Column
    private String address;

    @Column
    private String municipality;

    @Column
    private String town;

    @Column
    private String floors;

    @ElementCollection
    private List<String> images;

    @ElementCollection
    private List<String> documentation;

    @Column
    private RealEstateRequestStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "real_estate_request_id")
    private List<HouseholdRequest> householdRequests;

    @Column
    private Date createdAt;

    @Column
    private Date approvedAt;

    @Column
    private String adminNote;
}
