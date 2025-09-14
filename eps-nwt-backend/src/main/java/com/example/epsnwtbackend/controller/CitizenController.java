package com.example.epsnwtbackend.controller;

import com.example.epsnwtbackend.dto.CacheablePage;
import com.example.epsnwtbackend.dto.CitizenSearchDTO;
import com.example.epsnwtbackend.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/citizen")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    @GetMapping(path = "/search")
    public ResponseEntity<CacheablePage<CitizenSearchDTO>> search(
            @RequestParam(value = "username", required = false, defaultValue = "") String username,
            Pageable pageable) {
        CacheablePage<CitizenSearchDTO> users = citizenService.search(username, pageable);
        return ResponseEntity.ok(users);
    }
}
