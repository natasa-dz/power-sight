package com.example.epsnwtbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class OwnershipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String householdId;
    String userId;
    private Status status;
    @ElementCollection
    private List<String> images;

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    @ElementCollection
    private List<String> documents;

    // note: reason nullable, ukoliko status REJECTED!
    private String reason;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OwnershipRequest(){}

    public OwnershipRequest(Long id, String householdId, String userId, Status status, String reason, LocalDateTime submittedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.householdId = householdId;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "OwnershipRequest{" +
                "id=" + id +
                ", householdId='" + householdId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", images=" + images +
                ", documents=" + documents +
                ", reason='" + reason + '\'' +
                ", submittedAt=" + submittedAt +
                ", updatedAt=" + updatedAt +
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
