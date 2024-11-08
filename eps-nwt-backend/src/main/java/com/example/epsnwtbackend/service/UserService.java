package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.UserCredentials;
import com.example.epsnwtbackend.dto.UserDto;
import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private EmailService emailService; // Your email service
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${superadmin.password.filepath}") // Set this in your application.properties
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
    //todo: STUDENT 1
    // 4.2 --> registration x login [activation link] --> image compression x resizing x async
    // save it to .txt, export filePath via app.prop or config,
    // filePath set in userManual

    private String generateRandomPassword(){
        int length = 12; // Length of the password
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
            // Create directories if they don't exist
            File file = new File(passwordFilePath);
            file.getParentFile().mkdirs(); // Create the parent directories

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFilePath))) {
                writer.write(password);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
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
            superAdmin.setPasswordChanged(false); // Set initial password change requirement
            userRepository.save(superAdmin);
            savePasswordToFile(randomPassword);
        }

    }

    public String getUserPhotoPath(Long userId) {
        return userRepository.findUserPhotoPathById(userId);
    }

    public Optional<UserDto> findUser(String email){
        Optional<User> toFind = userRepository.findByUsername(email);
        if(toFind.isPresent()){
            return Optional.of(new UserDto(toFind.get()));
        }
        return Optional.empty();
    }


    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // Old password is incorrect
        }

        // Set the new password and mark the password as changed
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);  // Save the updated user to persist the changes

        return true;
    }

    private String generateSecret() {
        return new GoogleAuthenticator().createCredentials().getKey();
    }

    public Optional<UserCredentials> register(UserDto dto) {

        System.out.println(dto.getPassword());
        //TODO: Add missing fields!!!!

//        System.out.println(dto.getConfirmPassword());
//
//        if(!dto.getPassword().equals(dto.getConfirmPassword())){
//            return Optional.empty();
//        }
        if(userRepository.findByUsername(dto.getUsername()).isPresent()){
            return Optional.empty();
        }

        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));

        User user=new User(dto);

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

    public void saveActivationToken(String username, String activationToken) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        user.setActivationToken(activationToken);
        user.setActive(false);

        userRepository.save(user);
    }


}
