package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.OwnershipRequest;
import com.example.epsnwtbackend.model.Status;
import com.example.epsnwtbackend.repository.OwnershipRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnershipRequestService {
    @Autowired
    private OwnershipRequestRepository ownershipRequestRepository;

    @Autowired
    private EmailService emailService;

    public List<OwnershipRequest> getPendingRequests() {
        return ownershipRequestRepository.findByStatus(Status.PENDING);
    }

    public void processRequest(Long requestId, boolean approved, String reason) {
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

        ownershipRequestRepository.save(request);
    }

}
