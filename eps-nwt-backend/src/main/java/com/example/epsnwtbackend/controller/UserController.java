package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.ChangePasswordDto;
import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.dto.UserTokenState;
import com.example.epsnwtbackend.model.*;
import com.example.epsnwtbackend.service.EmailService;
import com.example.epsnwtbackend.service.EmployeeService;
import com.example.epsnwtbackend.service.UserService;
import com.example.epsnwtbackend.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController {

    @Autowired
    UserService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenUtils tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    EmployeeService employeeService;

    private static final String PHOTO_PATH = "/var/www/photos/profiles/";
    @Autowired
    private UserService userService;
    @PostMapping("/{userId}/upload_photo")
    public ResponseEntity<String> uploadUserPhoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException {
        String userFolder = PHOTO_PATH + "user_" + userId;
        File dir = new File(userFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "photo.jpg");
        file.transferTo(photo);

        // Save the photo path in the database, e.g., "/photos/profiles/user_<userId>/photo.jpg"
        String photoPath = "/photos/profiles/user_" + userId + "/photo.jpg";
        // Update user photo path in the database here...

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

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credentials.getEmail(), credentials.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Optional<UserDto> dto = userDetailsService.findUser(credentials.getEmail());

        String jwt = tokenService.generateToken(dto.get().getUsername(), dto.get().getRole(), dto.get().getId());
        int expiresIn = tokenService.getExpiredIn();
        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCredentials> registerProcess(@RequestBody UserDto dto){
        Optional<UserCredentials> credentials = userDetailsService.register(dto);

        if (credentials.isPresent()){
            System.out.println("Usao u credentialsPresent!!!");

            tokenService.generateToken(dto.getUsername(), dto.getRole(), dto.getId());

            String activationToken = UUID.randomUUID().toString();
            userService.saveActivationToken(dto.getUsername(), activationToken); // Save token and set user as inactive

            if (dto.getRole() == Role.EMPLOYEE) {
                User user = userService.findWholeUser(dto.getUsername());
                Employee employee = new Employee();
                employee.setUser(user);
                employee.setName(dto.getName());
                employee.setSurname(dto.getSurname());
                employeeService.saveEmployee(employee);
            }

            // generate unique activation token and send activation email as well
            String activationLink = "http://localhost:8080/users/api/auth/activate?token=" + activationToken;
            emailService.sendActivationEmail(dto.getUsername(), activationLink);
            return ResponseEntity.status(HttpStatus.OK).body(credentials.get());

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }


    @PostMapping(path = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCredentials> registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestParam("userPhoto") MultipartFile userPhoto,
            @RequestParam(value = "userData", required = false) String userDataJson) {

        try {
            String uploadDir = "uploads/";  // Directory to store user photos
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdir();
            }

            // Generate unique file name based on the username
            String fileName = username + "_profile.jpg";
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, userPhoto.getBytes());  // Save the photo to the file system

            // Create a new UserDto (or User entity if you have one) with the path to the saved image
            UserDto dto = new UserDto(username, password, Role.valueOf(role), filePath.toString(), false,true, ""); // Assume 'false' for isActive

            // Register user credentials
            Optional<UserCredentials> credentials = userDetailsService.register(dto);

            if (credentials.isPresent()) {
                // Generate and save activation token
                String activationToken = UUID.randomUUID().toString();
                userService.saveActivationToken(dto.getUsername(), activationToken);  // Save token and set user as inactive

                if (dto.getRole() == Role.EMPLOYEE) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    System.out.println("userDataJson is " + userDataJson);
                    System.out.println("userDataJson is " + dto.getId());
                    if(userDataJson == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    AdditionalDataUser userData = objectMapper.readValue(userDataJson, AdditionalDataUser.class);
                    User user = userService.findWholeUser(dto.getUsername());
                    Employee employee = new Employee();
                    employee.setUser(user);
                    employee.setName(userData.getName());
                    employee.setSurname(userData.getSurname());
                    employeeService.saveEmployee(employee);
                }

                String activationLink = "http://localhost:4200/activate?token=" + activationToken;
                emailService.sendActivationEmail(dto.getUsername(), activationLink);

                return ResponseEntity.status(HttpStatus.OK).body(credentials.get());
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // Handle errors like file storage issues
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // In case registration fails
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
            return ResponseEntity.ok().build(); // Status 200 (OK), with no body
            //
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "/byId/{userId}")
    public User getUserById(@PathVariable("userId")Long userId){
        return userService.getUserById(userId);
    }
}
