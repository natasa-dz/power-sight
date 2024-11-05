package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.dto.UserTokenState;
import com.example.epsnwtbackend.service.EmailService;
import com.example.epsnwtbackend.service.UserService;
import com.example.epsnwtbackend.utils.TokenUtils;
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


    // TODO: login i register

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTokenState> logInProcess(@RequestBody UserCredentials credentials){


        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credentials.getEmail(), credentials.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Optional<UserDto> dto = userDetailsService.findUser(credentials.getEmail());
        UserDto userDto=dto.get();


        String jwt = tokenService.generateToken(credentials.getEmail(), dto.get().getRole());
        int expiresIn = tokenService.getExpiredIn();
        System.out.println(tokenService.generateToken(credentials.getEmail(), dto.get().getRole()));
        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCredentials> registerProcess(@RequestBody UserDto dto, @RequestParam("recaptchaToken") String recaptchaToken){
        Optional<UserCredentials> credentials = userDetailsService.register(dto);

        if (credentials.isPresent()){
            tokenService.generateToken(dto.getUsername(), dto.getRole());

            String activationToken = UUID.randomUUID().toString();
            userService.saveActivationToken(dto.getUsername(), activationToken); // Save token and set user as inactive

            // generate unique activation token and send activation email as well
            String activationLink = "http://localhost:8080/api/auth/activate?token=" + activationToken;
            emailService.sendActivationEmail(dto.getUsername(), activationLink);
            return ResponseEntity.status(HttpStatus.OK).body(credentials.get());


        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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

}
