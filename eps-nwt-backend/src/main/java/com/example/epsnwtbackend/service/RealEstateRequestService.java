package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.CreateRealEstateRequestDTO;
import com.example.epsnwtbackend.enums.RealEstateRequestStatus;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.repository.CityRepository;
import com.example.epsnwtbackend.repository.MunicipalityRepository;
import com.example.epsnwtbackend.repository.RealEstateRequestRepository;
import com.example.epsnwtbackend.utils.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RealEstateRequestService {

    @Value("src/main/resources/data/requests/realEstate")
    private String dataDirPath;

    @Autowired
    private RealEstateRequestRepository repository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    public RealEstateRequest createRequest(CreateRealEstateRequestDTO requestDTO,
                                           List<HouseholdRequest> householdRequests){
        RealEstateRequest request = new RealEstateRequest();
        request.setOwner(requestDTO.getOwner());
        request.setAddress(requestDTO.getAddress());
        request.setMunicipality(requestDTO.getMunicipality());
        request.setTown(requestDTO.getTown());
        request.setFloors(requestDTO.getFloors());
        request.setCreatedAt(requestDTO.getCreatedAt());
        request.setStatus(RealEstateRequestStatus.CREATED);
        request.setHouseholdRequests(householdRequests);
        request.setApprovedAt(null);
        request.setAdminNote(null);
        request.setImages(null);
        request.setDocumentation(null);
        repository.save(request);
        return request;
    }

    public void saveData(RealEstateRequest request, List<String> imagePaths, List<String> docPaths){
        request.setImages(imagePaths);
        request.setDocumentation(docPaths);
        repository.save(request);
    }

    public String uploadRealEstateImage(long requestId, MultipartFile image) throws IOException {
        String fileName = StringUtils.cleanPath(image.getOriginalFilename());
        String uploadDir = StringUtils.cleanPath(dataDirPath + requestId + "/images");
        ImageUploadUtil.saveImage(uploadDir, fileName, image);
        return uploadDir+"/"+fileName;
    }

    public String uploadRealEstateDoc(long requestId, MultipartFile doc) throws IOException {
        String fileName = StringUtils.cleanPath(doc.getOriginalFilename());
        String uploadDir = StringUtils.cleanPath(dataDirPath + requestId + "/docs");
        ImageUploadUtil.saveImage(uploadDir, fileName, doc);
        return uploadDir+"/"+fileName;
    }

    public Map<String, List<String>> getCitiesWithMunicipalities() {
        Map<String, List<String>> citiesWithMunicipalities = new HashMap<>();
        List<City> cities = cityRepository.findAll();
        for (City city : cities) {
            citiesWithMunicipalities.put(city.getName(), municipalityRepository.findForCity(city.getId()));
        }
        System.out.println(citiesWithMunicipalities);
        return citiesWithMunicipalities;
    }

}
