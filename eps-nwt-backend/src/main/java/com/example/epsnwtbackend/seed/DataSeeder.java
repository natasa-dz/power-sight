package com.example.epsnwtbackend.seed;

import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.service.DataSeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private DataSeedService dataSeedService;

    @Override
    public void run(String... args) {
        //seedUsers(1000, 10, 4);
        //dataSeedService.seedRealEstateRequests(1000);
    }


    public void seedUsers(int citizenCount, int employeeCount, int adminCount){

        for (int i = 1; i <= citizenCount; i++) {
            UserDto dto = new UserDto("citizen" + i + "@test.com", "test", Role.CITIZEN, true, true, "");
            dataSeedService.registerCitizen(dto, true);
        }

        for (int i = 1; i <= employeeCount; i++) {
            UserDto dto = new UserDto("employee" + i + "@test.com", "test", Role.EMPLOYEE, true, true, "");
            dataSeedService.registerEmployee(dto, true);
        }

        for (int i = 1; i <= adminCount; i++) {
            UserDto dto = new UserDto("admin" + i + "@test.com", "admin", Role.ADMIN, true, true, "");
            dataSeedService.registerAdmin(dto, true);
        }
    }

}
