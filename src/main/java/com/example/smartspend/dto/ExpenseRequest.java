package com.example.smartspend.dto;

import java.time.LocalDate;

import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ExpenseRequest {
    @NotBlank
    private String title;
    
    @NotNull
    @Positive
    private Double amount;

    @NotNull
    private Category category;

    @NotNull
    private TransactionType type;

    @NotNull
    private LocalDate date;

    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}