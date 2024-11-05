package com.example.epsnwtbackend.model;

import com.example.epsnwtbackend.dto.UserDto;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String userPhoto;

    private String secret;

    //TODO: Kreiraj konstruktore do kraja!
    // Default constructor
    public User() {
    }

    // Constructor to create User from UserDto
    public User(UserDto dto) {
        this.username = dto.getUsername();
        this.password = dto.getPassword(); // Ensure password is encoded before setting
        this.role = dto.getRole();
        this.userPhoto = dto.getUserPhoto();
        this.isActive = false; // Set to false initially, activated via token
        this.passwordChanged = false; // Default to false, updated after first login change
        this.activationToken = dto.getActivationToken(); // Set if generated in service
    }
    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    @Column(nullable = false)
    private boolean passwordChanged = false;

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(name = "activation_token")
    private String activationToken;


    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Household> getHouseholds() {
        return households;
    }

    public void setHouseholds(Set<Household> households) {
        this.households = households;
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Household> households;


    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }


}
