package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")  // Allow requests from your frontend
@RequestMapping("/users/auth")
public class ActivationController {

    @Autowired
    private UserRepository userRepository;

    @PatchMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        System.out.println("Token sa front-a: "+token);
        User user = userRepository.findByActivationToken(token);
        System.out.println("Pronadjeni user: "+user.toString());
        if (!user.isActive()) {
            user.setActive(true);
            user.setActivationToken(null);
            System.out.println("User iz if-a: "+user.toString());
            userRepository.save(user);
            return ResponseEntity.ok("Account activated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired activation token.");
        }
    }
}
