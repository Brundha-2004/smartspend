package com.example.smartspend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.User;
import com.example.smartspend.service.BudgetService;
import com.example.smartspend.service.UserService;

@RestController
@RequestMapping("/budgets")
@SuppressWarnings("null")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<Budget>> getUserBudgets(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<Budget> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }
    
    @PostMapping
    public ResponseEntity<Budget> createBudget(@AuthenticationPrincipal UserDetails userDetails, 
                                             @RequestBody Budget budget) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Budget createdBudget = budgetService.createBudget(userId, budget);
        return ResponseEntity.ok(createdBudget);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long id,
                                             @RequestBody Budget budgetDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Budget updatedBudget = budgetService.updateBudget(userId, id, budgetDetails);
        return ResponseEntity.ok(updatedBudget);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long id) {
        Long userId = getUserIdFromUserDetails(userDetails);
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok("Budget deleted successfully");
    }
    
    private Long getUserIdFromUserDetails(@NonNull UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}