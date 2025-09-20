package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.*;
import com.example.epsnwtbackend.model.HouseholdRequest;
import com.example.epsnwtbackend.model.RealEstateRequest;
import com.example.epsnwtbackend.service.HouseholdRequestService;
import com.example.epsnwtbackend.service.RealEstateRequestService;
import com.example.epsnwtbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/real-estate-request")
public class RealEstateRequestController {

    @Autowired
    private RealEstateRequestService service;

    @Autowired
    private HouseholdRequestService householdRequestService;

    @Autowired
    private UserService userService;

    @Value("${app.upload.base}")
    private String basePath;

    @PostMapping(value = "/registration", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> createRequest(@RequestPart("images") Collection<MultipartFile> imageFiles,
                                                @RequestPart("documentation") Collection<MultipartFile> docFiles,
                                                @RequestPart("realEstateRequest") CreateRealEstateRequestDTO realEstateRequestDTO) throws IOException {
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
        return service.getCitiesWithMunicipalities();
    }

    @GetMapping(value = "/{ownerId}/all")
    public List<AllRealEstateRequestsDTO> getAllForOwner(@PathVariable("ownerId")Long ownerId){
        return service.getAllForOwner(ownerId);
    }

    @GetMapping(value = "/admin/requests")
    public List<AllRealEstateRequestsDTO> getAllForAdmin(){
        return service.getAllForAdmin();
    }

    @GetMapping(value = "/admin/request/{requestId}")
    public RealEstateRequestDTO getRequestForAdmin(@PathVariable("requestId")Long requestId){
        return service.getRequestForAdmin(requestId);
    }

    @GetMapping("/documentation/{realEstateId}")
    public List<String> getDocumentationByRequestId(@PathVariable Long realEstateId) {
        return service.getDocumentationForRealEstate(realEstateId);
    }

//    @GetMapping("/images/{realEstateId}")
//    public ResponseEntity<List<String>> getImagesByRealEstateId(@PathVariable("realEstateId") String realEstateId) {
//        Path imageDirectory = Paths.get("..","uploads", "realEstate", String.valueOf(realEstateId), "images");
//        try {
//            Files.createDirectories(imageDirectory);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not create upload directory: " + imageDirectory, e);
//        }
//        List<String> base64Images = new ArrayList<>();
//
//        try {
//            if (!Files.exists(imageDirectory) || !Files.isDirectory(imageDirectory)) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            Files.list(imageDirectory)
//                    .filter(Files::isRegularFile)
//                    .forEach(imagePath -> {
//                        try {
//                            byte[] imageBytes = Files.readAllBytes(imagePath);
//                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//                            base64Images.add(base64Image);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//
//            return ResponseEntity.ok(base64Images);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

//    @PostMapping(value = "/docs")
//    public ResponseEntity<byte[]> getDocsByRealEstateId(@RequestBody String filePath) {
//
//        try {
//            Path path = Paths.get(filePath).normalize();
//            if (!Files.exists(path) || !Files.isRegularFile(path)) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//
//            byte[] fileBytes = Files.readAllBytes(path);
//            String contentType = Files.probeContentType(path);
//            if (contentType == null) {
//                contentType = "application/pdf";
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .body(fileBytes);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @PostMapping(value = "/docs")
    public ResponseEntity<String> getDocsByRealEstateId(@RequestBody String filePath) {
        System.out.println(filePath);

        Path path = Paths.get(basePath).resolve(filePath).normalize();

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String url = "/uploads/" + path.toString().replace("\\", "/")
                .replaceFirst("^.*/uploads/", "");  // skida sve ispred "uploads/"

        return ResponseEntity.ok(url);
    }



    @GetMapping("/images/{realEstateId}")
    public ResponseEntity<List<String>> getImageUrlsByRealEstateId(@PathVariable("realEstateId") String realEstateId) {
        Path imageDirectory = Paths.get(basePath, "realEstate", String.valueOf(realEstateId), "images");

        try {
            if (!Files.exists(imageDirectory) || !Files.isDirectory(imageDirectory)) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<String> imageUrls = Files.list(imageDirectory)
                    .filter(Files::isRegularFile)
                    .map(imagePath -> "/uploads/realEstate/" + realEstateId + "/images/" + imagePath.getFileName().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(imageUrls);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PutMapping(value = "admin/finish/{requestId}")
    public ResponseEntity<String> finishRequest(@PathVariable("requestId")Long requestId,
                                                @RequestBody FinishRealEstateRequestDTO finishedRequest){
        if(!finishedRequest.getApproved()){
            if (finishedRequest.getNote() == null || finishedRequest.getNote().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Admin note is required for denied requests.");
            }
        } else {
            if (finishedRequest.getNote().equals("\"\"") || finishedRequest.getNote().isEmpty()){
                finishedRequest.setNote(null);
            }
        }
        Optional<UserDto> userDto = userService.findUser(finishedRequest.getOwner());
        int updated = service.finishRequest(requestId, finishedRequest.getApproved(), finishedRequest.getNote(), finishedRequest.getOwner(), userDto.get().getId());
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
