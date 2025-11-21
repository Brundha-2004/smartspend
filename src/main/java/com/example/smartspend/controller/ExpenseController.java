package com.example.smartspend.controller;

import java.time.LocalDate;
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

import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.Expense;
import com.example.smartspend.service.ExpenseService;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    // ========== HTML VIEW ENDPOINTS ==========
    
    // Show main expenses page (HTML)
    @GetMapping("/view")
    public String showExpensesPage(Model model) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            List<Expense> expenses = expenseService.getUserExpenses(1L);
            model.addAttribute("expenses", expenses);
            model.addAttribute("totalExpenses", expenses.size());
            
            // Calculate totals
            double totalExpenseAmount = expenseService.getTotalExpenses(1L, 
                LocalDate.now().withDayOfMonth(1), LocalDate.now());
            double totalIncomeAmount = expenseService.getTotalIncome(1L, 
                LocalDate.now().withDayOfMonth(1), LocalDate.now());
            
            model.addAttribute("totalExpenseAmount", String.format("%.2f", totalExpenseAmount));
            model.addAttribute("totalIncomeAmount", String.format("%.2f", totalIncomeAmount));
            model.addAttribute("netAmount", String.format("%.2f", totalIncomeAmount - totalExpenseAmount));
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading expenses: " + e.getMessage());
        }
        
        return "expenses";
    }
    
    // Show expense details page (HTML)
    @GetMapping("/view/{expenseId}")
    public String showExpenseDetails(@PathVariable Long expenseId, Model model) {
        try {
            Expense expense = expenseService.getExpenseById(expenseId);
            model.addAttribute("expense", expense);
            
        } catch (Exception e) {
            model.addAttribute("error", "Expense not found: " + e.getMessage());
            return "redirect:/expenses/view";
        }
        
        return "expense-details";
    }
    
    // ========== REST API ENDPOINTS ==========
    
    // Get all expenses (JSON)
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Expense>> getAllExpenses() {
        try {
            List<Expense> expenses = expenseService.getAllExpenses();
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get expenses for specific user (JSON)
    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserExpenses(@PathVariable Long userId) {
        try {
            List<Expense> expenses = expenseService.getUserExpenses(userId);
            return ResponseEntity.ok(expenses);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Get expense by ID (JSON)
    @GetMapping("/{expenseId}")
    @ResponseBody
    public ResponseEntity<?> getExpenseById(@PathVariable Long expenseId) {
        try {
            Expense expense = expenseService.getExpenseById(expenseId);
            return ResponseEntity.ok(expense);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    // Create new expense (JSON)
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createExpense(@RequestBody Expense expense) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            Expense createdExpense = expenseService.createExpense(1L, expense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expense created successfully");
            response.put("expenseId", createdExpense.getId());
            response.put("expense", createdExpense);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Update existing expense (JSON)
    @PutMapping("/{expenseId}")
    @ResponseBody
    public ResponseEntity<?> updateExpense(
            @PathVariable Long expenseId, 
            @RequestBody Expense expenseDetails) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            Expense updatedExpense = expenseService.updateExpense(1L, expenseId, expenseDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expense updated successfully");
            response.put("expenseId", updatedExpense.getId());
            response.put("expense", updatedExpense);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Delete expense (JSON)
    @DeleteMapping("/{expenseId}")
    @ResponseBody
    public ResponseEntity<?> deleteExpense(@PathVariable Long expenseId) {
        try {
            // For now using hardcoded userId - in real app, get from authenticated user
            expenseService.deleteExpense(1L, expenseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expense deleted successfully");
            response.put("expenseId", expenseId);
            response.put("status", "deleted");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "failed");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Get filtered expenses (JSON)
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<?> getFilteredExpenses(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            Category cat = category != null ? Category.valueOf(category.toUpperCase()) : null;
            
            List<Expense> expenses = expenseService.getFilteredExpenses(userId, start, end, cat, minAmount, maxAmount);
            return ResponseEntity.ok(expenses);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error filtering expenses");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Get totals (JSON)
    @GetMapping("/totals")
    @ResponseBody
    public ResponseEntity<?> getTotals(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            Double totalExpenses = expenseService.getTotalExpenses(userId, start, end);
            Double totalIncome = expenseService.getTotalIncome(userId, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalExpenses", totalExpenses);
            response.put("totalIncome", totalIncome);
            response.put("netSavings", totalIncome - totalExpenses);
            response.put("startDate", start);
            response.put("endDate", end);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error calculating totals");
            errorResponse.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Test endpoint (JSON)
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testExpense() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Expense endpoint working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    // Global exception handler
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