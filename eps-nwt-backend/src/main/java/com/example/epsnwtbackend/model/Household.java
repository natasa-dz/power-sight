package com.example.epsnwtbackend.model;


import jakarta.persistence.*;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Meter getMeters() {
        return meters;
    }

    public void setMeters(Meter meters) {
        this.meters = meters;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Meter meters;

    @Column(nullable = false)
    private String address;
}
