package com.example.smartspend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smartspend.entity.Category;
import com.example.smartspend.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Expense> findByUserIdAndCategory(Long userId, Category category);
    
    List<Expense> findByUserIdAndDateBetweenAndCategory(Long userId, LocalDate startDate, LocalDate endDate, Category category);
    
    List<Expense> findByUserIdAndAmountBetween(Long userId, Double minAmount, Double maxAmount);
    
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND " +
           "(:startDate IS NULL OR e.date >= :startDate) AND " +
           "(:endDate IS NULL OR e.date <= :endDate) AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:minAmount IS NULL OR e.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR e.amount <= :maxAmount)")
    List<Expense> findFilteredExpenses(@Param("userId") Long userId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("category") Category category,
                                      @Param("minAmount") Double minAmount,
                                      @Param("maxAmount") Double maxAmount);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = 'EXPENSE' AND e.category = :category AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    Double getTotalExpenseByCategoryAndMonth(@Param("userId") Long userId, 
                                           @Param("category") Category category,
                                           @Param("year") int year, 
                                           @Param("month") int month);
}