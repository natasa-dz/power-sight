package com.example.epsnwtbackend.model;

import com.example.epsnwtbackend.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements UserDetails {

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


    @Column(name = "user_photo" )  // Make it nullable if user photo is optional
    private String userPhoto;

    private String secret;

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

    @Column(nullable = false)
    private boolean passwordChanged = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(name = "activation_token")
    private String activationToken;

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Household> households;

    /*@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RealEstateRequest> requests;*/


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", userPhoto='" + userPhoto + '\'' +
                ", secret='" + secret + '\'' +
                ", passwordChanged=" + passwordChanged +
                ", isActive=" + isActive +
                ", activationToken='" + activationToken + '\'' +
                ", households=" + households +
                '}';
    }
}
