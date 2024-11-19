package com.example.epsnwtbackend.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/auth")
public class ActivationController {

    @Autowired
    private UserRepository userRepository;
    @PreAuthorize("permitAll()")
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam String token) {
        System.out.println("Token sa front-a: " + token);
        User user = userRepository.findByActivationToken(token);
        System.out.println("Pronadjeni user: " + user.toString());
        if (!user.isActive()) {
            user.setActive(true);
            user.setActivationToken(null);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
