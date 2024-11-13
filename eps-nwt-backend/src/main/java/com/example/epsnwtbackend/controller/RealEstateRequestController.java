package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.CreateRealEstateRequestDTO;
import com.example.epsnwtbackend.dto.HouseholdRequestDTO;
import com.example.epsnwtbackend.model.HouseholdRequest;
import com.example.epsnwtbackend.model.RealEstateRequest;
import com.example.epsnwtbackend.service.HouseholdRequestService;
import com.example.epsnwtbackend.service.RealEstateRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/real-estate-request")
public class RealEstateRequestController {

    @Autowired
    private RealEstateRequestService service;

    @Autowired
    private HouseholdRequestService householdRequestService;

    @PostMapping(value = "/registration", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> createRequest(@RequestPart("images") Collection<MultipartFile> imageFiles,
                                                @RequestPart("documentation") Collection<MultipartFile> docFiles,
                                                @RequestPart("realEstateRequest") CreateRealEstateRequestDTO realEstateRequestDTO) throws IOException {
        System.out.println("pogadja metodu");
        if (imageFiles.isEmpty()) {
            return new ResponseEntity<>("Missing real estate images!", HttpStatus.BAD_REQUEST);
        }
        if (docFiles.isEmpty()) {
            return new ResponseEntity<>("Missing documentation!", HttpStatus.BAD_REQUEST);
        }
        if (realEstateRequestDTO.getHouseholdRequests().isEmpty()) {
            return new ResponseEntity<>("You must create at least one household!", HttpStatus.BAD_REQUEST);
        }

        List<HouseholdRequest>householdRequests = new ArrayList<>();
        List<String>imagePaths = new ArrayList<>();
        List<String>docPaths =  new ArrayList<>();
        for(HouseholdRequestDTO householdRequestDTO : realEstateRequestDTO.getHouseholdRequests()){
            householdRequests.add(householdRequestService.createHouseholdRequest(householdRequestDTO));
        }
        RealEstateRequest realEstateRequest = service.createRequest(realEstateRequestDTO, householdRequests);
        for(MultipartFile image : imageFiles){
            imagePaths.add(service.uploadRealEstateImage(realEstateRequest.getId(), image));
        }
        for(MultipartFile doc : docFiles) {
            docPaths.add(service.uploadRealEstateDoc(realEstateRequest.getId(), doc));
        }
        service.saveData(realEstateRequest, imagePaths, docPaths);

        return new ResponseEntity<>("Real estate request created successfully!", HttpStatus.OK);
    }
}
