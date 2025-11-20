package com.example.smartspend.dto;

public class CategorySpending {
    private String name;
    private Double amount;
    private Double percentage;
    
    // Constructors
    public CategorySpending() {}
    
    public CategorySpending(String name, Double amount, Double percentage) {
        this.name = name;
        this.amount = amount;
        this.percentage = percentage;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}