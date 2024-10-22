package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.Role;
import com.example.epsnwtbackend.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.epsnwtbackend.repository.UserRepository;

import java.io.BufferedWriter;
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
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${superadmin.password.filepath}") // Set this in your application.properties
    private String passwordFilePath;

    @PostConstruct
    public void init() {
        createSuperAdminIfNotExists();
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

    private void savePasswordToFile(String password){

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFilePath))) {
            writer.write(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found")
        );

        return (UserDetails) user;
    }


    private void createSuperAdminIfNotExists() {
        if (userRepository.findByUsername("admin") == null) {
            String randomPassword = generateRandomPassword();
            User superAdmin = new User();
            superAdmin.setPassword(bCryptPasswordEncoder.encode(randomPassword));
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setPasswordChanged(false); // Set initial password change requirement

            userRepository.save(superAdmin);
            savePasswordToFile(randomPassword);
        }

    }

    //TODO: PROMENI U USERDTO, SOLVE PIC STORING LOGIC!
    public User registerUser(User registrationDto){
        // Create the user and save to database (initially inactive)
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(registrationDto.getPassword()));
        user.setActive(false); // User is not active by default

        // Generate activation token
        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        userRepository.save(user);

        // Send activation email
        String activationLink = "http://your-domain.com/activate?token=" + token;
        emailService.sendActivationEmail(user.getUsername(), activationLink);

        return user;
    }

}
