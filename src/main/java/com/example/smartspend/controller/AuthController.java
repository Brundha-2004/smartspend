package com.example.smartspend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartspend.dto.AuthRequest;
import com.example.smartspend.dto.AuthResponse;
import com.example.smartspend.dto.RegisterRequest;
import com.example.smartspend.entity.User;
import com.example.smartspend.security.JwtUtil;
import com.example.smartspend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@SuppressWarnings("null")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User(request.getEmail(), request.getPassword(), 
                               request.getFirstName(), request.getLastName());
            
            userService.registerUser(user);
            
            return ResponseEntity.ok("User registered successfully. Please check your email for verification.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.isEnabled()) {
                return ResponseEntity.badRequest().body("Please verify your email first");
            }
            
            String jwt = jwtUtil.generateToken(request.getEmail());
            
            AuthResponse response = new AuthResponse(jwt, user.getEmail(), 
                                                   user.getFirstName(), user.getLastName());
            
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@NonNull @RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok("Email verified successfully. You can now login.");
        }
        return ResponseEntity.badRequest().body("Invalid verification token");
    }
}