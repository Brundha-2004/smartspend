package com.example.smartspend.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartspend.entity.User;
import com.example.smartspend.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    public User registerUser(@NonNull User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);
        
        User savedUser = userRepository.save(user);
        
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        
        return savedUser;
    }
    
    public boolean verifyUser(@NonNull String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public Optional<User> findByEmail(@NonNull String email) {
        return userRepository.findByEmail(email);
    }
    
    public User findById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // ADD THIS MISSING METHOD
    public boolean checkPassword(@NonNull String rawPassword, @NonNull String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    public User updateUser(User user) {
    return userRepository.save(user);
}
}