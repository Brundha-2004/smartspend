package com.example.smartspend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.smartspend.dto.AuthRequest;
import com.example.smartspend.dto.AuthResponse;
import com.example.smartspend.dto.RegisterRequest;
import com.example.smartspend.entity.User;
import com.example.smartspend.security.JwtUtil;
import com.example.smartspend.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // ========== HTML PAGE ENDPOINTS ==========
    
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "registered", required = false) String registered,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please log in.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(@RequestParam(value = "error", required = false) String error,
                                  Model model) {
        if (error != null) {
            model.addAttribute("error", "Registration failed. Please try again.");
        }
        return "register";
    }
    
    // ========== HTML FORM PROCESSING ENDPOINTS ==========
    
    @PostMapping("/register")
public String handleRegisterForm(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
    try {
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "Passwords do not match");
            return "redirect:/auth/register";
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        User registeredUser = userService.registerUser(user);
        
        // For development: Auto-enable the user (remove in production)
        registeredUser.setEnabled(true);
        userService.updateUser(registeredUser);
        
        redirectAttributes.addAttribute("registered", "true");
        return "redirect:/auth/login";
            
    } catch (RuntimeException e) {
        redirectAttributes.addAttribute("error", e.getMessage());
        return "redirect:/auth/register";
    }
}
    
    @PostMapping("/login")
    public String handleLoginForm(@RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!userService.checkPassword(password, user.getPassword())) {
                redirectAttributes.addAttribute("error", "Invalid credentials");
                return "redirect:/auth/login";
            }
            
            if (!user.isEnabled()) {
                redirectAttributes.addAttribute("error", "Please verify your email first");
                return "redirect:/auth/login";
            }
            
            // Generate JWT token (you can store it in session/cookie later)
            String jwt = jwtUtil.generateToken(email);
            
            // For now, redirect to home page with success
            return "redirect:/?login=success";
            
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", "Invalid credentials");
            return "redirect:/auth/login";
        }
    }
    
    // ========== REST API ENDPOINTS (for AJAX/API calls) ==========
    
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<?> registerApi(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            User registeredUser = userService.registerUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", registeredUser.getId());
            response.put("email", registeredUser.getEmail());
            response.put("status", "pending_verification");
            
            return ResponseEntity.ok(response);
                
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@Valid @RequestBody AuthRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!userService.checkPassword(request.getPassword(), user.getPassword())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid credentials");
                errorResponse.put("status", "failed");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (!user.isEnabled()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please verify your email first");
                errorResponse.put("status", "unverified");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String jwt = jwtUtil.generateToken(request.getEmail());
            AuthResponse response = new AuthResponse(jwt, user.getEmail(), 
                                                   user.getFirstName(), user.getLastName());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verifyEmail(@NonNull @RequestParam String token) {
        if (userService.verifyUser(token)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            response.put("status", "verified");
            return ResponseEntity.ok(response);
        }
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid verification token");
        errorResponse.put("status", "failed");
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String testAuth() {
        return "Auth endpoint working - " + System.currentTimeMillis();
    }
}