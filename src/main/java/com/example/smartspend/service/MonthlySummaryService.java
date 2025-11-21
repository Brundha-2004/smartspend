package com.example.smartspend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class MonthlySummaryService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    public void generateAndSendMonthlySummary(@NonNull Long userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<Expense> monthlyExpenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        
        Double totalIncome = monthlyExpenses.stream()
                .filter(e -> e.getType().name().equals("INCOME"))
                .mapToDouble(Expense::getAmount)
                .sum();
        
        Double totalExpenses = monthlyExpenses.stream()
                .filter(e -> e.getType().name().equals("EXPENSE"))
                .mapToDouble(Expense::getAmount)
                .sum();
        
        List<Budget> monthlyBudgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        List<EmailService.BudgetStatus> budgetStatusList = new ArrayList<>();
        
        for (Budget budget : monthlyBudgets) {
            Double spent = expenseRepository.getTotalExpenseByCategoryAndMonth(userId, budget.getCategory(), year, month);
            if (spent == null) spent = 0.0;
            Double utilization = (spent / budget.getAmount()) * 100;
            
            budgetStatusList.add(new EmailService.BudgetStatus(
                budget.getCategory(), 
                budget.getAmount(), 
                spent, 
                utilization
            ));
        }
        
        Map<Category, Double> categorySpending = monthlyExpenses.stream()
                .filter(e -> e.getType().name().equals("EXPENSE"))
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.summingDouble(Expense::getAmount)
                ));
        
        Double totalSpending = categorySpending.values().stream().mapToDouble(Double::doubleValue).sum();
        
        List<EmailService.CategorySpending> topCategories = categorySpending.entrySet().stream()
                .map(entry -> new EmailService.CategorySpending(
                    entry.getKey().toString(),
                    entry.getValue(),
                    totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0.0
                ))
                .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
                .limit(5)
                .collect(Collectors.toList());
        
        User user = userService.findById(userId);
        emailService.sendMonthlySummaryEmail(user.getEmail(), month, year, totalIncome, totalExpenses, 
                                           budgetStatusList, topCategories);
    }
}