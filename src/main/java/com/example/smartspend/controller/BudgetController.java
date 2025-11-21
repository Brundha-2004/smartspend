package com.example.smartspend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.service.BudgetService;

@Controller
@RequestMapping("/budgets")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    // ========== HTML VIEW ENDPOINTS ==========
    
    // Show main budgets page (HTML)
    @GetMapping("/view")
    public String showBudgetsPage(Model model) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            List<Budget> budgets = budgetService.getUserBudgets(1L);
            model.addAttribute("budgets", budgets);
            model.addAttribute("totalBudgets", budgets.size());
            
            // Calculate total amount
            double totalAmount = budgets.stream()
                    .mapToDouble(Budget::getAmount)
                    .sum();
            model.addAttribute("totalAmount", String.format("%.2f", totalAmount));
            
            // Calculate active budgets (budgets for current month)
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            int currentYear = java.time.LocalDate.now().getYear();
            long activeBudgets = budgets.stream()
                    .filter(b -> b.getMonth().equals(currentMonth) && b.getYear().equals(currentYear))
                    .count();
            model.addAttribute("activeBudgets", activeBudgets);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading budgets: " + e.getMessage());
        }
        
        return "budgets";
    }
    
    // Show budget details page (HTML)
    @GetMapping("/view/{budgetId}")
    public String showBudgetDetails(@PathVariable Long budgetId, Model model) {
        try {
            Budget budget = budgetService.getBudgetById(budgetId);
            model.addAttribute("budget", budget);
            
            // Calculate utilization (placeholder)
            double utilization = calculateBudgetUtilization(budget);
            model.addAttribute("utilization", utilization);
            
            // Calculate remaining amount
            double remaining = budget.getAmount() * (1 - (utilization / 100));
            model.addAttribute("remaining", String.format("%.2f", remaining));
            
        } catch (Exception e) {
            model.addAttribute("error", "Budget not found: " + e.getMessage());
            return "redirect:/budgets/view";
        }
        
        return "budget-details";
    }
    
    // ========== REST API ENDPOINTS ==========
    
    // Get all budgets (JSON)
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Budget>> getAllBudgets() {
        try {
            List<Budget> budgets = budgetService.getAllBudgets();
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get budgets for specific user (JSON)
    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserBudgets(@PathVariable Long userId) {
        try {
            List<Budget> budgets = budgetService.getUserBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Get budget by ID (JSON)
    @GetMapping("/{budgetId}")
    @ResponseBody
    public ResponseEntity<?> getBudgetById(@PathVariable Long budgetId) {
        try {
            Budget budget = budgetService.getBudgetById(budgetId);
            return ResponseEntity.ok(budget);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    // Create new budget (JSON)
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createBudget(@RequestBody Budget budget) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            Budget createdBudget = budgetService.createBudget(1L, budget);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Budget created successfully");
            response.put("budgetId", createdBudget.getId());
            response.put("budget", createdBudget);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Update existing budget (JSON)
    @PutMapping("/{budgetId}")
    @ResponseBody
    public ResponseEntity<?> updateBudget(
            @PathVariable Long budgetId, 
            @RequestBody Budget budgetDetails) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            Budget updatedBudget = budgetService.updateBudget(1L, budgetId, budgetDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Budget updated successfully");
            response.put("budgetId", updatedBudget.getId());
            response.put("budget", updatedBudget);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Delete budget (JSON)
    @DeleteMapping("/{budgetId}")
    @ResponseBody
    public ResponseEntity<?> deleteBudget(@PathVariable Long budgetId) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            budgetService.deleteBudget(1L, budgetId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Budget deleted successfully");
            response.put("budgetId", budgetId);
            response.put("status", "deleted");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Get budgets by category (JSON) - FIXED
    @GetMapping("/category/{category}")
    @ResponseBody
    public ResponseEntity<?> getBudgetsByCategory(@PathVariable String category) {
        try {
            List<Budget> budgets = budgetService.getBudgetsByCategory(category);
            return ResponseEntity.ok(budgets);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching budgets by category");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Get budgets for specific month and year (JSON) - FIXED
    @GetMapping("/period")
    @ResponseBody
    public ResponseEntity<?> getBudgetsByPeriod(
            @RequestParam Integer month, 
            @RequestParam Integer year) {
        try {
            List<Budget> budgets = budgetService.getBudgetsByUserIdAndMonthAndYear(1L, month, year);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching budgets for period");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Get total budget amount for user in specific period (JSON) - FIXED
    @GetMapping("/total")
    @ResponseBody
    public ResponseEntity<?> getTotalBudgetAmount(
            @RequestParam Integer month, 
            @RequestParam Integer year) {
        try {
            Double totalAmount = budgetService.getTotalBudgetAmountByUserAndMonth(1L, month, year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", 1L);
            response.put("month", month);
            response.put("year", year);
            response.put("totalBudgetAmount", totalAmount != null ? totalAmount : 0.0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error calculating total budget amount");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Check if budget exists for category and period (JSON) - FIXED
    @GetMapping("/exists")
    @ResponseBody
    public ResponseEntity<?> checkBudgetExists(
            @RequestParam String category,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        try {
            boolean exists = budgetService.existsByUserIdAndCategoryAndMonthAndYear(1L, category, month, year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("category", category);
            response.put("month", month);
            response.put("year", year);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error checking budget existence");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Test endpoint (JSON)
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testBudget() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Budget endpoint working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    // Health check endpoint (JSON)
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Budget Service");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    // Utility method for budget utilization calculation
    private double calculateBudgetUtilization(Budget budget) {
        // Implement your actual budget utilization logic here
        // This should calculate based on actual expenses vs budget
        // For now, returning a placeholder value
        return Math.min(75.0, 100.0); // Example: 75% utilized, max 100%
    }
    
    // Global exception handler for this controller
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal server error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", "error");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}