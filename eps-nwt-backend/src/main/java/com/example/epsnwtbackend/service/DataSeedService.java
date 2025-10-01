package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.CreateRealEstateRequestDTO;
import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataSeedService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitizenService citizenService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    private RealEstateRequestService realEstateRequestService;

    public Optional<User> registerBase(UserDto dto, boolean forceActive) {

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            return Optional.empty();
        }

        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));

        User user = new User(dto);

        if (user.getRole() == Role.ADMIN || forceActive) {
            user.setActive(true);
            user.setActivationToken(null);
        }

        User saved = userRepository.save(user);
        return Optional.of(saved);
    }


    public void registerCitizen(UserDto dto, boolean forceActive) {
        registerBase(dto, forceActive).ifPresent(user -> {
            Citizen citizen = new Citizen();
            citizen.setUser(user);
            citizen.setUsername(user.getUsername());
            citizenService.saveCitizen(citizen);
        });
    }

    public void registerEmployee(UserDto dto, boolean forceActive) {
        registerBase(dto, forceActive).ifPresent(user -> {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setName("Name" + user.getId());
            employee.setSurname("Surname" + user.getId());
            employee.setSuspended(false);
            employee.setUsername(user.getUsername());
            employeeService.saveEmployee(employee);
        });
    }

    public void registerAdmin(UserDto dto, boolean forceActive) {
        registerBase(dto, forceActive);
    }

    public void seedRealEstateRequests(int count) {
        List<User> citizens = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole().equals(Role.CITIZEN))
                .toList();

        Random random = new Random();

        for (int i = 0; i < count; i++) {
            User citizen = citizens.get(random.nextInt(citizens.size()));

            List<HouseholdRequest> householdRequests = new ArrayList<>();
            int householdCount = 1 + random.nextInt(3);
            for (int h = 0; h < householdCount; h++) {
                HouseholdRequest hr = new HouseholdRequest();
                hr.setFloor(h + 1);
                hr.setSquareFootage((float) (40 + random.nextInt(80)));
                hr.setApartmentNumber(h + 1);
                householdRequests.add(hr);
            }

            CreateRealEstateRequestDTO dto = new CreateRealEstateRequestDTO();
            dto.setOwner(citizen.getId());
            dto.setAddress("Seed Street " + i);
            dto.setMunicipality("Municipality " + (i % 5));
            dto.setTown("Town " + (i % 3));
            dto.setFloors(1 + random.nextInt(5));
            dto.setCreatedAt(new Date());

            RealEstateRequest request = realEstateRequestService.createRequest(dto, householdRequests);

            boolean approved = random.nextBoolean();
            String note = approved ? "Approved by seeder" : "Rejected by seeder";

            realEstateRequestService.finishSeedRequest(
                    request.getId(),
                    approved,
                    note,
                    citizen.getUsername(),
                    citizen.getId()
            );
        }
    }
}
