package com.example.smartspend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smartspend.entity.Budget;
import com.example.smartspend.entity.Category;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findByUserId(Long userId);
    
    // UPDATED THESE METHODS TO USE Category ENUM
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            Long userId, Category category, Integer month, Integer year);
    
    boolean existsByUserIdAndCategoryAndMonthAndYear(
            Long userId, Category category, Integer month, Integer year);
    
    List<Budget> findByCategory(Category category);
    
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndCategory(Long userId, Category category);
    
    // Get budgets for a specific month and year
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    
    // Get total budget amount for a user in specific month/year
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.user.id = :userId AND b.month = :month AND b.year = :year")
    Double getTotalBudgetAmountByUserAndMonth(@Param("userId") Long userId, 
                                             @Param("month") Integer month, 
                                             @Param("year") Integer year);
}