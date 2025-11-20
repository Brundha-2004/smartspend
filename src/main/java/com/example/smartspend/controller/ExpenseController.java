package com.example.smartspend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.Expense;
import com.example.smartspend.entity.User;
import com.example.smartspend.service.ExpenseService;
import com.example.smartspend.service.UserService;

@RestController
@RequestMapping("/expenses")
@SuppressWarnings("null")
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<Expense> expenses = expenseService.getFilteredExpenses(userId, startDate, endDate, category, minAmount, maxAmount);
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/total-expenses")
    public ResponseEntity<Double> getTotalExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        Double total = expenseService.getTotalExpenses(userId, startDate, endDate);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/total-income")
    public ResponseEntity<Double> getTotalIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        Double total = expenseService.getTotalIncome(userId, startDate, endDate);
        return ResponseEntity.ok(total);
    }
    
    private Long getUserIdFromUserDetails(@NonNull UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}