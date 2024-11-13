package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ProcessRequestDto;
import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.service.OwnershipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ownership-requests")
public class OwnershipRequestController {
    @Autowired
    private OwnershipRequestService ownershipRequestService;

    @GetMapping("/pending")
    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestService.getPendingRequests();
    }

    @PostMapping("/process/{id}")
    public ResponseEntity<?> processRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDto dto) {

        ownershipRequestService.processRequest(id, dto.isApproved(), dto.getReason());
        return ResponseEntity.ok("Request processed successfully");
    }
}
