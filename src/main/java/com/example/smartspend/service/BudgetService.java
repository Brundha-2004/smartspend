package com.example.smartspend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.Expense;
import com.example.smartspend.entity.User;
import com.example.smartspend.repository.BudgetRepository;
import com.example.smartspend.repository.ExpenseRepository;

@Service
@Transactional
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }
    
    public List<Budget> getUserBudgets(@NonNull Long userId) {
        return budgetRepository.findByUserId(userId);
    }
    
    public Budget getBudgetById(@NonNull Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }
    
    public Budget createBudget(@NonNull Long userId, @NonNull Budget budget) {
        User user = userService.findById(userId);
        
        // Check if budget already exists for this category and period
        if (budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(
                userId, budget.getCategory(), budget.getMonth(), budget.getYear())) {
            throw new RuntimeException("Budget already exists for this category and month");
        }
        
        budget.setUser(user);
        return budgetRepository.save(budget);
    }
    
    public Budget updateBudget(@NonNull Long userId, @NonNull Long budgetId, @NonNull Budget budgetDetails) {
        Budget budget = getBudgetById(budgetId);
        
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        budget.setAmount(budgetDetails.getAmount());
        budget.setCategory(budgetDetails.getCategory());
        budget.setMonth(budgetDetails.getMonth());
        budget.setYear(budgetDetails.getYear());
        
        return budgetRepository.save(budget);
    }
    
    public void deleteBudget(@NonNull Long userId, @NonNull Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        budgetRepository.delete(budget);
    }
    
    // Get budgets by category
    public List<Budget> getBudgetsByCategory(@NonNull String categoryString) {
        try {
            // Convert String to Category enum
            Category category = Category.valueOf(categoryString.toUpperCase());
            return budgetRepository.findByCategory(category);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryString);
        }
    }
    
    public List<Budget> getBudgetsByUserIdAndMonthAndYear(@NonNull Long userId, @NonNull Integer month, @NonNull Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
    }
    
    public Double getTotalBudgetAmountByUserAndMonth(@NonNull Long userId, @NonNull Integer month, @NonNull Integer year) {
        return budgetRepository.getTotalBudgetAmountByUserAndMonth(userId, month, year);
    }
    
    public boolean existsByUserIdAndCategoryAndMonthAndYear(@NonNull Long userId, @NonNull String categoryString, 
                                                          @NonNull Integer month, @NonNull Integer year) {
        try {
            // Convert String to Category enum
            Category category = Category.valueOf(categoryString.toUpperCase());
            return budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(userId, category, month, year);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryString);
        }
    }
    
    public Optional<Budget> findById(@NonNull Long id) {
        return budgetRepository.findById(id);
    }
    
    // Additional utility methods
    public boolean existsByUserIdAndCategory(@NonNull Long userId, @NonNull String categoryString) {
        try {
            Category category = Category.valueOf(categoryString.toUpperCase());
            return budgetRepository.existsByUserIdAndCategory(userId, category);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryString);
        }
    }
    
    public Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(@NonNull Long userId, @NonNull String categoryString, 
                                                                 @NonNull Integer month, @NonNull Integer year) {
        try {
            Category category = Category.valueOf(categoryString.toUpperCase());
            return budgetRepository.findByUserIdAndCategoryAndMonthAndYear(userId, category, month, year);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryString);
        }
    }
    
    // BUDGET ALERTS METHOD - ADD THIS METHOD
    public void checkBudgetAlerts(@NonNull Long userId, @NonNull Expense expense) {
        try {
            // Only check for expense transactions (not income)
            if (!expense.getType().name().equals("EXPENSE")) {
                return;
            }
            
            int month = expense.getDate().getMonthValue();
            int year = expense.getDate().getYear();
            
            // Get budget for this category and period
            Optional<Budget> budgetOpt = findByUserIdAndCategoryAndMonthAndYear(
                    userId, expense.getCategory().name(), month, year);
            
            if (budgetOpt.isPresent()) {
                Budget budget = budgetOpt.get();
                
                // Use the repository method to get total expenses for this category and month
                Double totalSpent = expenseRepository.getTotalExpenseByCategoryAndMonth(
                        userId, expense.getCategory(), year, month);
                
                if (totalSpent != null && budget.getAmount() > 0) {
                    double utilization = (totalSpent / budget.getAmount()) * 100;
                    
                    User user = userService.findById(userId);
                    
                    // Send alerts based on utilization
                    if (utilization >= 80 && utilization < 100) {
                        // Budget warning (80-99% utilized)
                        System.out.println("=== BUDGET WARNING ALERT ===");
                        System.out.println("User: " + user.getEmail());
                        System.out.println("Category: " + budget.getCategory());
                        System.out.println("Budget: $" + budget.getAmount());
                        System.out.println("Spent: $" + totalSpent);
                        System.out.println("Utilization: " + String.format("%.1f", utilization) + "%");
                        System.out.println("============================");
                        
                        // Send email alert
                        emailService.sendBudgetWarningEmail(user.getEmail(), budget, utilization);
                        
                    } else if (utilization >= 100) {
                        // Budget exceeded (100%+ utilized)
                        System.out.println("=== BUDGET EXCEEDED ALERT ===");
                        System.out.println("User: " + user.getEmail());
                        System.out.println("Category: " + budget.getCategory());
                        System.out.println("Budget: $" + budget.getAmount());
                        System.out.println("Spent: $" + totalSpent);
                        System.out.println("Utilization: " + String.format("%.1f", utilization) + "%");
                        System.out.println("Overspent: $" + String.format("%.2f", (totalSpent - budget.getAmount())));
                        System.out.println("=============================");
                        
                        // Send email alert
                        emailService.sendBudgetExceededEmail(user.getEmail(), budget, utilization);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking budget alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}