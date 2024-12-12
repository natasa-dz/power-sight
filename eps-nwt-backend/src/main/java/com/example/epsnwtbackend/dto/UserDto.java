package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.model.User;

import java.io.File;

public class UserDto {

    private Long id;
    private String username;
    private Role role;
    private String userPhoto;  // This could be a URL pointing to the photo location
    private boolean isActive;

    private String password;
    private String activationToken;

    private boolean passwordChanged;

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

    public UserDto(String username, String password, Role role, boolean isActive, boolean passwordChanged, String activationToken) {
        this.username = username;
        this.role = role;
        this.password = password;
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


    // Getters and Setters
    public Long getId() {
        return id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged=passwordChanged;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }
}