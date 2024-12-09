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

import javax.swing.text.Document;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ownership-requests")
public class OwnershipRequestController {
    @Autowired
    private OwnershipRequestService ownershipRequestService;

    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @GetMapping("/pending")
    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestService.getPendingRequests();
    }

    @PostMapping("/process/{id}")
    public ResponseEntity<?> processRequest(
            @PathVariable Long id,
            @RequestBody ProcessRequestDto dto) throws MessagingException {

        ownershipRequestService.processRequest(id, dto.isApproved(), dto.getReason());
        return ResponseEntity.ok("Request processed successfully");
    }


    //todo: submit ownership request
    @PostMapping("/requestOwnership")
    public ResponseEntity<?> submitOwnershipRequest(@RequestParam String userId,@RequestParam Long householdId, @RequestParam List<MultipartFile> documents) {
        OwnershipRequest request = new OwnershipRequest();
        //request.setHousehold(householdRepository.findById(householdId).orElseThrow());
        request.setUserId(userId);
        request.setStatus(Status.PENDING);
        request.setSubmittedAt(LocalDateTime.now());

        //List<Document> savedDocs = documentService.saveAll(documents);
        //request.setDocuments(savedDocs);
        ownershipRequestRepository.save(request);

        return ResponseEntity.ok("Ownership request submitted.");
    }
}
