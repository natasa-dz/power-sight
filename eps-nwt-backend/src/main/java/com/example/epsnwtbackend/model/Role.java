package com.example.epsnwtbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN, EMPLOYEE, CITIZEN, SUPERADMIN;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return this.name();
    }
}
