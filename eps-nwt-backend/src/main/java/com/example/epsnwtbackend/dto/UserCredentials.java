package com.example.epsnwtbackend.dto;

import com.example.epsnwtbackend.model.User;

public class UserCredentials {

    private String email;
    private String password;

    private String secret;


    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public UserCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserCredentials(UserDto user){
        this.email = user.getUsername();
        this.password = user.getPassword();
    }
    public UserCredentials(User user){
        this.email=  user.getUsername();
        this.password = user.getPassword();
        this.secret=user.getSecret();
    }


}
