package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ProcessRequestDto;
import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import com.example.epsnwtbackend.repository.OwnershipRequestRepository;
import com.example.epsnwtbackend.service.OwnershipRequestService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.Path;

import javax.swing.text.Document;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ownership-requests")
public class OwnershipRequestController {
    @Autowired
    private OwnershipRequestService ownershipRequestService;

    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @PostMapping("/process/{id}")
    public ResponseEntity<?> processRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDto dto) throws MessagingException {

        ownershipRequestService.processRequest(id, dto.isApproved(), dto.getReason());
        return ResponseEntity.ok("Request processed successfully");
    }

    @PostMapping("/requestOwnership")
    public ResponseEntity<?> submitOwnershipRequest(@RequestParam String userId,@RequestParam Long householdId, @RequestParam List<MultipartFile> files) {
        OwnershipRequest request = new OwnershipRequest();
        request.setHouseholdId(householdId);
        request.setUserId(userId);
        request.setStatus(Status.PENDING);
        request.setSubmittedAt(LocalDateTime.now());

        OwnershipRequest savedRequest = ownershipRequestRepository.save(request);

        String storagePath = ownershipRequestService.storeFiles(request.getId(), files);
        savedRequest.setDocumentationPath(storagePath);


        ownershipRequestRepository.save(savedRequest);


        return ResponseEntity.ok("Ownership request submitted.");
    }

    @GetMapping("/{userId}")
    public List<OwnershipRequest> getUserOwnershipRequests(@PathVariable String userId){
        return ownershipRequestService.getUserOwnershipRequests(userId);

    }

    @GetMapping("/pending")
    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestService.getPendingRequests();
    }


}
