package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.AllRealEstateRequestsDTO;
import com.example.epsnwtbackend.dto.CreateRealEstateRequestDTO;
import com.example.epsnwtbackend.dto.RealEstateRequestDTO;
import com.example.epsnwtbackend.enums.RealEstateRequestStatus;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.repository.*;
import com.example.epsnwtbackend.utils.CyrillicConverter;
import com.example.epsnwtbackend.utils.ImageUploadUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.List;

@Service
public class RealEstateRequestService {

    @Value("${app.upload.real-estate-request}")
    private String dataDirPath;

    @Autowired
    private RealEstateRequestRepository repository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RealEstateRepository realEstateRepository;

    @CacheEvict(value = {"reRequests", "reRequestsForOwner"}, key = "#requestDTO.owner.id", allEntries = true)
    public RealEstateRequest createRequest(CreateRealEstateRequestDTO requestDTO,
                                           List<HouseholdRequest> householdRequests){
        RealEstateRequest request = new RealEstateRequest();
        request.setOwner(requestDTO.getOwner());
        String address = CyrillicConverter.toLatin2(requestDTO.getAddress());
        request.setAddress(address);
        request.setMunicipality(requestDTO.getMunicipality());
        request.setTown(requestDTO.getTown());
        request.setFloors(requestDTO.getFloors());
        request.setCreatedAt(requestDTO.getCreatedAt());
        request.setStatus(RealEstateRequestStatus.CREATED);
        request.setHouseholdRequests(householdRequests);
        request.setFinishedAt(null);
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

    @Cacheable("cityMunicipality")
    public Map<String, List<String>> getCitiesWithMunicipalities() {
        Map<String, List<String>> citiesWithMunicipalities = new HashMap<>();
        List<City> cities = cityRepository.findAll();
        for (City city : cities) {
            citiesWithMunicipalities.put(city.getName(), municipalityRepository.findForCity(city.getId()));
        }
        System.out.println(citiesWithMunicipalities);
        return citiesWithMunicipalities;
    }

    @Cacheable(
            value = "reRequestsForOwner",
            key = "#ownerId"
    )
    public List<AllRealEstateRequestsDTO> getAllForOwner(Long ownerId){
        List<RealEstateRequest> requests = repository.getAllForOwner(ownerId);
        List<AllRealEstateRequestsDTO> dtos = new ArrayList<>();
        for (RealEstateRequest r : requests) {
            dtos.add(new AllRealEstateRequestsDTO(r.getId(), r.getOwner(), r.getStatus(),
                    r.getCreatedAt(), r.getFinishedAt(), r.getAddress(), r.getMunicipality(), r.getTown()));
        }
        return dtos;
    }

    @Cacheable("reRequests")
    public List<AllRealEstateRequestsDTO> getAllForAdmin(){
        List<AllRealEstateRequestsDTO> dtos = new ArrayList<>();
        List<RealEstateRequest> requests = repository.findAllByStatus(RealEstateRequestStatus.CREATED);
        for (RealEstateRequest r : requests) {
            dtos.add(new AllRealEstateRequestsDTO(r.getId(), r.getOwner(), r.getStatus(),
                    r.getCreatedAt(), r.getFinishedAt(), r.getAddress(), r.getMunicipality(), r.getTown()));
        }
        return dtos;
    }

    @Cacheable(
            value = "reRequestForAdmin",
            key = "#requestId"
    )
    public RealEstateRequestDTO getRequestForAdmin(Long requestId){
        RealEstateRequest entity = repository.findById(requestId).orElseThrow();
        return RealEstateRequestDTO.mapToDTO(entity);
    }

    public List<String> getDocumentationForRealEstate (Long realEstateId){
        return repository.findById(realEstateId).get().getDocumentation();
    }

    @Caching(evict = {
            @CacheEvict(value = "reRequests", allEntries = true),
            @CacheEvict(value = "reRequestForAdmin", key = "#requestId"),
            @CacheEvict(value = "reRequestsForOwner", key = "#ownerId")
    })
    @Transactional
    public int finishRequest(Long requestId, Boolean approved, String adminNote, String owner, Long ownerId) {
        if (!approved && (adminNote == null || adminNote.trim().isEmpty())) {
            return 3;
        }
        RealEstateRequestStatus status = approved ? RealEstateRequestStatus.APPROVED : RealEstateRequestStatus.DENIED;
        int updatedRows = this.repository.finishRequest(requestId, status, adminNote);
        System.out.println(updatedRows);
        try {
            emailService.sendRegistrationRequestEmail(owner, adminNote, approved);
        } catch (MessagingException e) {
            System.out.println("email error");
            e.printStackTrace();
            return 2;
        }
        if (approved && updatedRows==1){
            createRealEstate(repository.findById(requestId).get());
        }
        // 0 - not updated in db
        // 1 - updated in db and email sent
        // 2 - updated in db and email not sent
        // 3 - bad note
        return updatedRows;
    }

    private void createRealEstate(RealEstateRequest request) {
        RealEstate realEstate = new RealEstate();
        realEstate.setAddress(request.getAddress());
        realEstate.setMunicipality(request.getMunicipality());
        realEstate.setTown(request.getTown());
        realEstate.setFloors(request.getFloors());
        realEstate.setImages(new ArrayList<>(request.getImages()));
        realEstateRepository.save(realEstate);

        User owner = userRepository.findById(request.getOwner()).get();
        List<Household> households = new ArrayList<>();
        for (HouseholdRequest hr : request.getHouseholdRequests()){
            households.add(createHousehold(hr, realEstate, owner));
        }
        realEstate.setHouseholds(households);
        realEstateRepository.save(realEstate);
    }

    @CacheEvict(value = {"householdDetails", "noOwnerHouseholds"}, allEntries = true)
    public Household createHousehold(HouseholdRequest request, RealEstate realEstate, User owner){
        Household household = new Household();
        household.setFloor(request.getFloor());
        household.setSquareFootage(request.getSquareFootage());
        household.setApartmentNumber(request.getApartmentNumber());
        household.setRealEstate(realEstate);
        household.setAccessGranted(new ArrayList<>());
        return household;
    }
}
