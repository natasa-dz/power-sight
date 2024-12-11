package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import com.example.epsnwtbackend.repository.OwnershipRequestRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OwnershipRequestService {
    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @Autowired
    private EmailService emailService;

    private final String baseDirectory = "data/requests/ownership";

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

    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestRepository.findByStatus(Status.PENDING);
    }

    public void processRequest(Long requestId, boolean approved, String reason) throws MessagingException {
        OwnershipRequest request = ownershipRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request ID"));

        if (!request.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Request has already been processed");
        }

        if (approved) {
            request.setStatus(Status.valueOf("APPROVED"));
            emailService.sendApprovalEmail(request.getUserId());
        } else {
            request.setStatus(Status.valueOf("REJECTED"));
            request.setReason(reason);
            emailService.sendRejectionEmail(request.getUserId(), reason);
        }

        request.setUpdatedAt(LocalDateTime.now());

        ownershipRequestRepository.save(request);
    }

    public List<OwnershipRequest> getUserOwnershipRequests(String username){
        return ownershipRequestRepository.findByUserId(username);
    }

}
