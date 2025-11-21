package com.example.smartspend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("status", "API Server Running");
        model.addAttribute("port", request.getServerPort());
        return "home";
    }
    
    @GetMapping("/info")
    public String info() {
        return "info";
    }
}