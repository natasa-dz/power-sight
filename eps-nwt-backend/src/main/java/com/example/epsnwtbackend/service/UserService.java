package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.model.RealEstateRequest;
import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.epsnwtbackend.repository.UserRepository;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${superadmin.password.filepath}")
    private String passwordFilePath;

    @PostConstruct
    public void init() {
        try {
            createSuperAdminIfNotExists();
        } catch (Exception e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateRandomPassword(){
        int length = 12;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();

    }

    private void savePasswordToFile(String password) {
        try {
            File file = new File(passwordFilePath);
            file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFilePath))) {
                writer.write(password);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(value = "userByUsername", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user.get();
        }
    }

    @PostConstruct
    private void createSuperAdminIfNotExists() {

        if (userRepository.findByUsername("admin@gmail.com").isEmpty()) {
            String randomPassword = generateRandomPassword();
            User superAdmin = new User();
            superAdmin.setPassword(bCryptPasswordEncoder.encode(randomPassword));
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setActive(true);
            superAdmin.setUsername("admin@gmail.com");
            superAdmin.setPasswordChanged(false);
            userRepository.save(superAdmin);
            savePasswordToFile(randomPassword);
        }

    }

    @Cacheable(value = "userPhoto", key = "#userId")
    public String getUserPhotoPath(Long userId) {
        return userRepository.findUserPhotoPathById(userId);
    }

    @Cacheable(value = "userByEmail", key = "#email")
    public Optional<UserDto> findUser(String email){
        System.out.println("dunjaaaaaa 33: "+email);
        Optional<User> toFind = userRepository.findByUsername(email);
        return toFind.map(UserDto::new);
    }

    public Optional<UserDto> findUserByToken(String token){
        User toFind = userRepository.findByActivationToken(token);
        if(toFind!=null){
            return Optional.of(new UserDto(toFind));
        }
        return Optional.empty();
    }


    @Caching(evict = {
            @CacheEvict(value = "userByEmail", key="#username"),
            @CacheEvict(value = "userByUsername", key="#username")
    })
    public boolean changePassword(String username, String confirmPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);

        return true;
    }

    private String generateSecret() {
        return new GoogleAuthenticator().createCredentials().getKey();
    }

    @Caching(evict = {
            @CacheEvict(value = "userByEmail", key="#username"),
            @CacheEvict(value = "userByUsername", key="#username")
    })
    public Optional<UserCredentials> register(UserDto dto, String username) {

        if(userRepository.findByUsername(dto.getUsername()).isPresent()){
            return Optional.empty();
        }

        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));

        User user=new User(dto);

        if(user.getRole() == Role.ADMIN){
            user.setActive(true);
            user.setActivationToken(null);
        }

        User saved = userRepository.save(user);
        return Optional.of(new UserCredentials(saved));
    }

    public Optional<UserDto> logIn(UserCredentials credentials) {
        Optional<User> u = userRepository.findByUsername(credentials.getEmail());
        if ((u.isPresent()) && (u.get().getPassword().equals(credentials.getPassword()))){
            return Optional.of(new UserDto(u.get()));
        }
        return Optional.empty();
    }

    public User findWholeUser(String email) {
        Optional<User> u = userRepository.findByUsername(email);
        if(u.isPresent()){return u.get();}
        throw new RuntimeException("User not found with email");
    }

    public void saveActivationToken(String username, String activationToken) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        user.setActivationToken(activationToken);
        user.setActive(false);

        userRepository.save(user);
    }

    @Cacheable(value = "userById", key = "#userId")
    public User getUserById(Long userId){
        return userRepository.findById(userId).get();
    }




}
