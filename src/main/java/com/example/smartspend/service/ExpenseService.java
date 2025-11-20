package com.example.smartspend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.Expense;
import com.example.smartspend.entity.User;
import com.example.smartspend.repository.ExpenseRepository;

@Service
@SuppressWarnings("null")
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BudgetService budgetService;
    
    public List<Expense> getUserExpenses(@NonNull Long userId) {
        return expenseRepository.findByUserId(userId);
    }
    
    public List<Expense> getFilteredExpenses(@NonNull Long userId, LocalDate startDate, LocalDate endDate, 
                                           Category category, Double minAmount, Double maxAmount) {
        return expenseRepository.findFilteredExpenses(userId, startDate, endDate, category, minAmount, maxAmount);
    }
    
    public Expense createExpense(@NonNull Long userId, @NonNull Expense expense) {
        User user = userService.findById(userId);
        expense.setUser(user);
        
        Expense savedExpense = expenseRepository.save(expense);
        
        // Check budget alerts
        budgetService.checkBudgetAlerts(userId, expense);
        
        return savedExpense;
    }
    
    public Expense updateExpense(@NonNull Long userId, @NonNull Long expenseId, @NonNull Expense expenseDetails) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        expense.setTitle(expenseDetails.getTitle());
        expense.setAmount(expenseDetails.getAmount());
        expense.setCategory(expenseDetails.getCategory());
        expense.setType(expenseDetails.getType());
        expense.setDate(expenseDetails.getDate());
        expense.setDescription(expenseDetails.getDescription());
        
        return expenseRepository.save(expense);
    }
    
    public void deleteExpense(@NonNull Long userId, @NonNull Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        expenseRepository.delete(expense);
    }
    
    public Double getTotalExpenses(@NonNull Long userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return expenses.stream()
                .filter(e -> e.getType().name().equals("EXPENSE"))
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    
    public Double getTotalIncome(@NonNull Long userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return expenses.stream()
                .filter(e -> e.getType().name().equals("INCOME"))
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}