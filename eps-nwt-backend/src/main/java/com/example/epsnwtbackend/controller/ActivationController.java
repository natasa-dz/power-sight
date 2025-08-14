package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")  // Allow requests from your frontend
@RequestMapping("/users/auth")
public class ActivationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

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
            Objects.requireNonNull(cacheManager.getCache("userByUsername")).evict(user.getUsername());
            Objects.requireNonNull(cacheManager.getCache("userByEmail")).evict(user.getUsername());
            Objects.requireNonNull(cacheManager.getCache("userById")).evict(user.getId());
            Objects.requireNonNull(cacheManager.getCache("userPhoto")).evict(user.getId());
            return ResponseEntity.ok("Account activated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired activation token.");
        }
    }
}
