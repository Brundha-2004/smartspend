package com.example.smartspend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.Expense;
import com.example.smartspend.entity.User;
import com.example.smartspend.repository.BudgetRepository;
import com.example.smartspend.repository.ExpenseRepository;

@Service
@SuppressWarnings("null")
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    public List<Budget> getUserBudgets(@NonNull Long userId) {
        return budgetRepository.findByUserId(userId);
    }
    
    public Budget createBudget(@NonNull Long userId, @NonNull Budget budget) {
        User user = userService.findById(userId);
        
        // Check if budget already exists for this category and month
        if (budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(
                userId, budget.getCategory(), budget.getMonth(), budget.getYear())) {
            throw new RuntimeException("Budget already exists for this category and month");
        }
        
        budget.setUser(user);
        return budgetRepository.save(budget);
    }
    
    public Budget updateBudget(@NonNull Long userId, @NonNull Long budgetId, @NonNull Budget budgetDetails) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        budget.setAmount(budgetDetails.getAmount());
        return budgetRepository.save(budget);
    }
    
    public void deleteBudget(@NonNull Long userId, @NonNull Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        budgetRepository.delete(budget);
    }
    
    public void checkBudgetAlerts(@NonNull Long userId, @NonNull Expense expense) {
        if (!expense.getType().name().equals("EXPENSE")) {
            return;
        }
        
        int month = expense.getDate().getMonthValue();
        int year = expense.getDate().getYear();
        
        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, expense.getCategory(), month, year);
        
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            Double totalSpent = expenseRepository.getTotalExpenseByCategoryAndMonth(
                    userId, expense.getCategory(), year, month);
            
            if (totalSpent != null) {
                double utilization = (totalSpent / budget.getAmount()) * 100;
                
                // Get user email first
                User user = userService.findById(userId);
                
                if (utilization >= 80 && utilization < 100) {
                    // Send warning email - pass email directly
                    emailService.sendBudgetWarningEmail(user.getEmail(), budget, utilization);
                } else if (utilization >= 100) {
                    // Send exceeded email - pass email directly
                    emailService.sendBudgetExceededEmail(user.getEmail(), budget, utilization);
                }
            }
        }
    }
}