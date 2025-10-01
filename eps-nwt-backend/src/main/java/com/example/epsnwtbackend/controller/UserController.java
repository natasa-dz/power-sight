package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ChangePasswordDto;
import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.dto.UserTokenState;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.service.*;
import com.example.epsnwtbackend.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost", allowCredentials = "true")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenUtils tokenService;

    @Autowired
    DataSeedService dataSeedService;


    @Autowired
    private EmailService emailService;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    private CitizenService citizenService;

    @Value("${app.upload.base}")
    private String uploadBase;

    @Value("${app.upload.photo-path}")
    private String photoPathBase;
    @Autowired
    private UserService userService;
    @PostMapping("/{userId}/upload_photo")
    public ResponseEntity<String> uploadUserPhoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException {
        String userFolder = photoPathBase + "user_" + userId;
        File dir = new File(userFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "photo.jpg");
        file.transferTo(photo);

        String photoPath = "/photos/profiles/user_" + userId + "/photo.jpg";

        return ResponseEntity.ok(photoPath);
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<String> getUserPhotoPath(@PathVariable Long userId) {
        String photoPath = userService.getUserPhotoPath(userId);

        if (photoPath == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(photoPath);
    }


    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTokenState> logInProcess(@RequestBody UserCredentials credentials){
        System.out.println("ime: "+credentials.getEmail());
        System.out.println("sifra: "+credentials.getPassword());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credentials.getEmail(), credentials.getPassword()));
        System.out.println("dunjaaaaaaaa ");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("dunjaaaaaaaa22 ");
        Optional<UserDto> dto = userDetailsService.findUser(credentials.getEmail());

        System.out.println("dto: "+dto.get().getUsername()+ " "+dto.get().getRole());

        if (dto.get().getRole() == Role.EMPLOYEE) {
            System.out.println("usao u employee");
            Employee employee = employeeService.getEmployeeByUserId(dto.get().getId());
            System.out.println("suspendovan: "+employee.getSuspended());
            System.out.println("employee email: "+employee.getUsername());
            if(employee.getSuspended()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String jwt = tokenService.generateToken(dto.get().getUsername(), dto.get().getRole(), dto.get().getId());
        System.out.println("jwt: "+jwt);
        int expiresIn = tokenService.getExpiredIn();
        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

//    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<UserCredentials> registerProcess(@RequestBody UserDto dto) throws MessagingException {
//        Optional<UserCredentials> credentials = userDetailsService.register(dto, dto.getUsername());
//
//        if (credentials.isPresent()){
//
//            tokenService.generateToken(dto.getUsername(), dto.getRole(), dto.getId());
//
//            String activationToken = UUID.randomUUID().toString();
//            userService.saveActivationToken(dto.getUsername(), activationToken);
//
//            if (dto.getRole() == Role.EMPLOYEE) {
//                User user = userService.findWholeUser(dto.getUsername());
//                Employee employee = new Employee();
//                employee.setUser(user);
//                employee.setName(dto.getName());
//                employee.setSurname(dto.getSurname());
//                employee.setSuspended(false);
//                employee.setUsername(user.getUsername());
//                employeeService.saveEmployee(employee);
//            } else if (dto.getRole() == Role.CITIZEN) {
//                User user = userService.findWholeUser(dto.getUsername());
//                Citizen citizen = new Citizen();
//                citizen.setUser(user);
//                citizen.setUsername(user.getUsername());
//                citizenService.saveCitizen(citizen);
//            }
//
//            String activationLink = "http://localhost:8080/users/api/auth/activate?token=" + activationToken;
//            emailService.sendActivationEmail(dto.getUsername(), activationLink);
//            return ResponseEntity.status(HttpStatus.OK).body(credentials.get());
//
//        }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//    }


    @PostMapping("/register-locust")
    public ResponseEntity<?> registerLocustUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "CITIZEN") String role,
            @RequestParam(required = false) MultipartFile userPhoto) {

        try {
            UserDto dto = new UserDto();
            dto.setUsername(username);
            dto.setPassword(password);
            dto.setRole(role.equalsIgnoreCase("EMPLOYEE") ? com.example.epsnwtbackend.model.Role.EMPLOYEE :
                    role.equalsIgnoreCase("ADMIN") ? com.example.epsnwtbackend.model.Role.ADMIN :
                            com.example.epsnwtbackend.model.Role.CITIZEN);

            // forceActive = true → odmah aktivan (za testove)
            if (dto.getRole().equals(com.example.epsnwtbackend.model.Role.CITIZEN)) {
                dataSeedService.registerCitizen(dto, true);
            } else if (dto.getRole().equals(com.example.epsnwtbackend.model.Role.EMPLOYEE)) {
                dataSeedService.registerEmployee(dto, true);
            } else {
                dataSeedService.registerAdmin(dto, true);
            }

            return ResponseEntity.ok("Locust user registered successfully: " + username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to register locust user: " + e.getMessage());
        }
    }

    @PostMapping(path = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCredentials> registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestParam("userPhoto") MultipartFile userPhoto,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "surname", required = false) String surname) {

        try {
            UserDto dto = new UserDto(username, password, Role.valueOf(role), false, true, "");
            Optional<UserCredentials> credentials = userDetailsService.register(dto, dto.getUsername());

            if (credentials.isPresent()) {
                User user = userService.findWholeUser(dto.getUsername());
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }

                String activationToken = UUID.randomUUID().toString();
                userService.saveActivationToken(dto.getUsername(), activationToken);

                Path uploadDir = Paths.get(uploadBase, "pictures");
                Files.createDirectories(uploadDir);

                String fileName = "profile_" + user.getId() + ".jpg";
                Path filePath = uploadDir.resolve(fileName);
                Files.write(filePath, userPhoto.getBytes());

                String fileUrl = uploadBase + "/pictures/" + fileName;
                dto.setUserPhoto(fileUrl);

                if (dto.getRole() == Role.EMPLOYEE) {
                    Employee employee = new Employee();
                    employee.setUser(user);
                    employee.setName(name);
                    employee.setSurname(surname);
                    employee.setSuspended(false);
                    employee.setUsername(user.getUsername());
                    employeeService.saveEmployee(employee);
                } else if (dto.getRole() == Role.CITIZEN) {
                    Citizen citizen = new Citizen();
                    citizen.setUser(user);
                    citizen.setUsername(user.getUsername());
                    citizenService.saveCitizen(citizen);
                }

                String activationLink = "http://localhost/activate?token=" + activationToken;
                emailService.sendActivationEmail(dto.getUsername(), activationLink);

                return ResponseEntity.ok(credentials.get());
            }

        } catch (IOException | MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.badRequest().body(null);
    }


    @PreAuthorize("permitAll()")
    @GetMapping(path = "/{email}")
    public ResponseEntity<UserDto> getUser(@PathVariable String email, HttpServletResponse response) {
        Optional<UserDto> retVal = userDetailsService.findUser(email);
        if (retVal.isPresent()) {
            return ResponseEntity.ok().body(retVal.get());
        }
        response.setHeader("Cache-Control", "no-store");
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping(path = "/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }


    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto) {
        boolean isChanged = userService.changePassword(dto.getUsername(), dto.getConfirmPassword(), dto.getNewPassword());

        if (isChanged) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "/byId/{userId}")
    public User getUserById(@PathVariable("userId")Long userId){
        return userService.getUserById(userId);
    }
}
