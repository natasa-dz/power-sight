package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.model.User;
import lombok.Data;

import java.io.File;

@Data
public class UserDto {

    private Long id;
    private String username;
    private Role role;
    private String userPhoto;  // This could be a URL pointing to the photo location
    private boolean isActive;

    private String password;
    private String activationToken;

    private boolean passwordChanged;

    private String name;

    private String surname;

    // Constructors
    public UserDto() {}

    public UserDto(String username, String password, Role role, String userPhoto, boolean isActive, boolean passwordChanged, String activationToken) {
        this.username = username;
        this.role = role;
        this.password = password;
        this.userPhoto = userPhoto;
        this.isActive = isActive;
        this.passwordChanged = passwordChanged;
        this.activationToken = activationToken;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", userPhoto='" + userPhoto + '\'' +
                ", isActive=" + isActive +
                ", password='" + password + '\'' +
                ", activationToken='" + activationToken + '\'' +
                ", passwordChanged=" + passwordChanged +
                '}';
    }

    // Constructor to initialize UserDto from User entity
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.userPhoto = user.getUserPhoto();
        this.isActive = user.isActive();
        this.passwordChanged = user.isPasswordChanged();
        this.activationToken = user.getActivationToken();  // Optional, include only if relevant
    }

    // Constructors

    public UserDto(String username, Role role, String userPhoto, boolean isActive, boolean passwordChanged) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.userPhoto = userPhoto;
        this.isActive = isActive;
        this.passwordChanged = passwordChanged;
    }
}