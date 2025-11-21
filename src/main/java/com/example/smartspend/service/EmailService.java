package com.example.smartspend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.Category;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;
    
    public void sendVerificationEmail(@NonNull String toEmail, @NonNull String token) {
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        
        if (!emailEnabled) {
            System.out.println("📧 [EMAIL DISABLED] Verification email would be sent to: " + toEmail);
            System.out.println("🔗 Verification URL: " + verificationUrl);
            System.out.println("🔑 Verification Token: " + token);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Verify your SmartSpend account");
            
            Context context = new Context();
            context.setVariable("verificationUrl", verificationUrl);
            String htmlContent = templateEngine.process("verification-email", context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Verification email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
            System.out.println("🔗 Manual verification URL: " + verificationUrl);
        }
    }
    
    public void sendBudgetWarningEmail(@NonNull String userEmail, @NonNull Budget budget, double utilization) {
        if (!emailEnabled) {
            System.out.println("📧 [EMAIL DISABLED] Budget warning for: " + userEmail);
            System.out.println("💰 Category: " + budget.getCategory() + ", Utilization: " + String.format("%.2f", utilization) + "%");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(userEmail);
            helper.setSubject("Budget Warning - " + budget.getCategory());
            
            Context context = new Context();
            context.setVariable("category", budget.getCategory());
            context.setVariable("budgetAmount", budget.getAmount());
            context.setVariable("utilization", String.format("%.2f", utilization));
            String htmlContent = templateEngine.process("budget-warning-email", context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send budget warning email: " + e.getMessage());
        }
    }
    
    public void sendBudgetExceededEmail(@NonNull String userEmail, @NonNull Budget budget, double utilization) {
        if (!emailEnabled) {
            System.out.println("📧 [EMAIL DISABLED] Budget exceeded for: " + userEmail);
            System.out.println("💰 Category: " + budget.getCategory() + ", Utilization: " + String.format("%.2f", utilization) + "%");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(userEmail);
            helper.setSubject("Budget Exceeded - " + budget.getCategory());
            
            Context context = new Context();
            context.setVariable("category", budget.getCategory());
            context.setVariable("budgetAmount", budget.getAmount());
            context.setVariable("utilization", String.format("%.2f", utilization));
            String htmlContent = templateEngine.process("budget-exceeded-email", context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send budget exceeded email: " + e.getMessage());
        }
    }
    
    public void sendMonthlySummaryEmail(@NonNull String userEmail, int month, int year, 
                                       Double totalIncome, Double totalExpenses, 
                                       List<BudgetStatus> budgetStatus, 
                                       List<CategorySpending> topCategories) {
        if (!emailEnabled) {
            System.out.println("📧 [EMAIL DISABLED] Monthly summary for: " + userEmail);
            System.out.println("📊 Month: " + month + "/" + year + ", Income: " + totalIncome + ", Expenses: " + totalExpenses);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(userEmail);
            helper.setSubject("Your Monthly Financial Summary - " + month + "/" + year);
            
            Context context = new Context();
            context.setVariable("month", month);
            context.setVariable("year", year);
            context.setVariable("totalIncome", totalIncome);
            context.setVariable("totalExpenses", totalExpenses);
            context.setVariable("netSavings", totalIncome - totalExpenses);
            context.setVariable("budgetStatus", budgetStatus);
            context.setVariable("topCategories", topCategories);
            context.setVariable("exceededBudgets", 
                budgetStatus.stream().filter(b -> b.getUtilization() >= 100).count());
            
            String htmlContent = templateEngine.process("monthly-summary-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send monthly summary email: " + e.getMessage());
        }
    }
    
    public boolean isEmailConfigured() {
        return emailEnabled;
    }
    
    public static class BudgetStatus {
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
    
    public static class CategorySpending {
        private String name;
        private Double amount;
        private Double percentage;
        
        public CategorySpending() {}
        
        public CategorySpending(String name, Double amount, Double percentage) {
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}