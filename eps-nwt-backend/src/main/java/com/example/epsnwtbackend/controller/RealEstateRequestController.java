package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.AllRealEstateRequestsDTO;
import com.example.epsnwtbackend.dto.CreateRealEstateRequestDTO;
import com.example.epsnwtbackend.dto.FinishRealEstateRequestDTO;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    @GetMapping(value = "")
    public Map<String, List<String>> getCitiesWithMunicipalities() {
        System.out.println("pogadja metodu");
        return service.getCitiesWithMunicipalities();
    }

    @GetMapping(value = "/{ownerId}/all")
    public List<AllRealEstateRequestsDTO> getAllForOwner(@PathVariable("ownerId")Long ownerId){
        System.out.println(ownerId + " hhhhhhhhhhhhhhhhhhhhhhhhhh");
        return service.getAllForOwner(ownerId);
    }

    @GetMapping(value = "/admin/requests")
    public List<AllRealEstateRequestsDTO> getAllForAdmin(){
        return service.getAllForAdmin();
    }

    @GetMapping(value = "/admin/request/{requestId}")
    public RealEstateRequest getRequestForAdmin(@PathVariable("requestId")Long requestId){
        return service.getRequestForAdmin(requestId);
    }

    @GetMapping("/images/{realEstateId}")
    public ResponseEntity<List<String>> getImagesByRealEstateId(@PathVariable("realEstateId") String realEstateId) {
        Path imageDirectory = Paths.get("src/main/resources/data/requests/realEstate" + realEstateId + "/images");
        List<String> base64Images = new ArrayList<>();

        try {
            if (!Files.exists(imageDirectory) || !Files.isDirectory(imageDirectory)) {
                return ResponseEntity.badRequest().body(null);
            }
            Files.list(imageDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(imagePath -> {
                        try {
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                            base64Images.add(base64Image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            return ResponseEntity.ok(base64Images);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/docs")
    public ResponseEntity<byte[]> getDocsByRealEstateId(@RequestBody String filePath) {

        System.out.println("usaoooo");
        try {
            Path path = Paths.get(filePath).normalize();
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] fileBytes = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/pdf";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "admin/finish/{requestId}")
    public ResponseEntity<String> finishRequest(@PathVariable("requestId")Long requestId,
                                                @RequestBody FinishRealEstateRequestDTO finishedRequest){
        System.out.println("gadja finish!!!!!!!!!!");
        if(!finishedRequest.getApproved()){
            if (finishedRequest.getNote() == null || finishedRequest.getNote().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Admin note is required for denied requests.");
            }
        } else {
            if (finishedRequest.getNote().equals("\"\"") || finishedRequest.getNote().isEmpty()){
                finishedRequest.setNote(null);
            }
        }

        int updated = service.finishRequest(requestId, finishedRequest.getApproved(), finishedRequest.getNote(), finishedRequest.getOwner());
        if (updated == 1) {
            return ResponseEntity.ok("Real estate request finished successfully!");
        } else if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Real estate request not found or not updated.");
        } else if (updated == 2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Real estate request finished.\nEmail not sent");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Admin note is required for denied requests.");
        }
    }
}
