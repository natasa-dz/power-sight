package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ProcessRequestDto;
import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.OwnershipRequestRepository;
import com.example.epsnwtbackend.repository.UserRepository;
import com.example.epsnwtbackend.service.OwnershipRequestService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import retrofit2.http.Path;

import javax.swing.text.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ownership-requests")
public class OwnershipRequestController {
    @Autowired
    private OwnershipRequestService ownershipRequestService;

    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/process")
    public ResponseEntity<?> processRequest(
            @RequestBody ProcessRequestDto dto) throws MessagingException, NoResourceFoundException {
        OwnershipRequest request = ownershipRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid request ID"));
        Optional<User> toFind = userRepository.findByUsername(request.getUserId());
        if(toFind.isPresent()) {
            ownershipRequestService.processRequest(dto.getRequestId(), dto.isApproved(), dto.getReason(), toFind.get().getUsername());
            return ResponseEntity.ok("Request processed successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/requestOwnership")
    @Caching(evict = {
            @CacheEvict(value = "pendingRequests", allEntries = true),
            @CacheEvict(value = "userOwnershipRequests", allEntries = true)
    })
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
