package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.model.User;
import com.example.epsnwtbackend.service.CitizenService;
import com.example.epsnwtbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/citizen")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;
    @Autowired
    private UserService userService;

    @Value("${app.upload.pictures}")
    private String picturesBase;

    @Value("${app.upload.pictures-prefix}")
    private String uploadUrlPrefix;

    @GetMapping(path = "/search")
    public ResponseEntity<CacheablePage<CitizenSearchDTO>> search(
            @RequestParam(value = "username", required = false, defaultValue = "") String username,
            Pageable pageable) {
        CacheablePage<CitizenSearchDTO> users = citizenService.search(username, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/image/{userId}")
    public ResponseEntity<String> getImageUrlsByUserId(@PathVariable("userId") Long userId) {
        Path imageDirectory = Paths.get(picturesBase);

        if (!Files.exists(imageDirectory) || !Files.isDirectory(imageDirectory)) {
            return ResponseEntity.ok(null);
        }

        String imageUrl = uploadUrlPrefix + "/profile_" + userId + ".jpg";

        return ResponseEntity.ok(imageUrl);
    }
}
