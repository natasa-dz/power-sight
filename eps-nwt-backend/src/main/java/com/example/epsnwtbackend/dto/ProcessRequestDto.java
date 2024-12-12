package com.example.epsnwtbackend.dto;

import org.springframework.web.bind.annotation.PathVariable;

public class ProcessRequestDto {

    private Long requestId;
    private boolean approved;
    private String reason;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public boolean isApproved() {
        return approved;
    }

    public ProcessRequestDto() {
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public ProcessRequestDto(boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
// Getters and setters
}
