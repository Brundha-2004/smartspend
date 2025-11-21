package com.example.smartspend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SmartSpend API");
        response.put("status", "running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        response.put("description", "Personal finance management API");
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "SmartSpend API");
        response.put("version", "1.0.0");
        response.put("database", "connected");
        response.put("environment", "production");
        return response;
    }
    
    @GetMapping("/test")
    public Map<String, Object> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test endpoint working!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");
        return response;
    }
}