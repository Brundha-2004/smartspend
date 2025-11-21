package com.example.smartspend.dto;

import com.example.smartspend.entity.Category;

public class BudgetStatus {
    private Category category;
    private Double amount;
    private Double spent;
    private Double utilization;
    
    public BudgetStatus() {}
    
    public BudgetStatus(Category category, Double amount, Double spent, Double utilization) {
        this.category = category;
        this.amount = amount;
        this.spent = spent;
        this.utilization = utilization;
    }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public Double getSpent() { return spent; }
    public void setSpent(Double spent) { this.spent = spent; }
    
    public Double getUtilization() { return utilization; }
    public void setUtilization(Double utilization) { this.utilization = utilization; }
}