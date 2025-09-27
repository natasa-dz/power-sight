package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.HouseholdRepository;
import com.example.epsnwtbackend.repository.OwnershipRequestRepository;
import com.example.epsnwtbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OwnershipRequestService {
    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private UserRepository userRepository;

    private final String baseDirectory = Paths.get("..","uploads", "requests", "house").toString();

    public String storeFiles(Long requestId, List<MultipartFile> files) {
        String folderPath = baseDirectory + requestId;
        Path folder = Paths.get(folderPath);

        try {
            Files.createDirectories(folder);
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                Path filePath = folder.resolve(fileName);
                Files.write(filePath, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("File upload failed for request ID: " + requestId, e);
        }
        return folder.toString();
    }

    @Cacheable(value = "pendingRequests")
    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestRepository.findByStatus(Status.PENDING);
    }
    @Caching(evict = {
            @CacheEvict(value = "pendingRequests", allEntries = true),
            @CacheEvict(value = "userOwnershipRequests", key = "#username"),
            @CacheEvict(value = "noOwnerHouseholds", allEntries = true)
    })
    public void processRequest(Long requestId, boolean approved, String reason, String username) throws MessagingException, NoResourceFoundException {
        OwnershipRequest request = ownershipRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request ID"));

        if (!request.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Request has already been processed");
        }

        if (approved) {
            request.setStatus(Status.valueOf("APPROVED"));
            Household household=householdRepository.getReferenceById(request.getHouseholdId());
            Optional<User> toFind = userRepository.findByUsername(request.getUserId());
            if(toFind.isPresent()){
                household.setOwner(toFind.get());
                householdRepository.save(household);
            }
            emailService.sendApprovalEmail(request.getUserId());
        } else {
            request.setStatus(Status.valueOf("REJECTED"));
            request.setReason(reason);
            emailService.sendRejectionEmail(request.getUserId(), reason);
        }

        request.setUpdatedAt(LocalDateTime.now());

        ownershipRequestRepository.save(request);
    }

    @Cacheable(value = "userOwnershipRequests", key="#username")
    public List<OwnershipRequest> getUserOwnershipRequests(String username){
        return ownershipRequestRepository.findByUserId(username);
    }

}
