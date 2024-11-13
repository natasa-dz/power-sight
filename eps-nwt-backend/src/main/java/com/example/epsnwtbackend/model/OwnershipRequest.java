package com.example.epsnwtbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class OwnershipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String householdId;
    String userId;
    private Status status;

    private String reason;

    public OwnershipRequest(){}
    public OwnershipRequest(Long id, String householdId, String userId, Status status, String reason) {
        this.id = id;
        this.householdId = householdId;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "OwnershipRequest{" +
                "id=" + id +
                ", householdId='" + householdId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }




}
